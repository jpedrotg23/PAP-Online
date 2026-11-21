package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.dto.AtendimentoRequest;
import com.br.unifor.paponline.dto.RemarcarAtendimentoRequest;
import com.br.unifor.paponline.entidades.AreaServico;
import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.entidades.HorarioDisponivel;
import com.br.unifor.paponline.repository.AreaServicoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.HorarioDisponivelRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private static final String AGENDADO = "AGENDADO";
    private static final String REALIZADO = "REALIZADO";
    private static final String CANCELADO = "CANCELADO";
    private static final String FALTA = "FALTA";
    private static final List<String> STATUS_EM_ABERTO = List.of(AGENDADO);
    private static final String MENSAGEM_BLOQUEIO_FALTAS =
            "Novos agendamentos suspensos por 3 faltas no semestre. Fale com a coordenação do PAP.";
    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");

    private final AtendimentoRepository repository;
    private final AreaServicoRepository areaRepository;
    private final EntityManager entityManager;
    private final HorarioDisponivelRepository horarioRepository;
    private final NotificacaoService notificacaoService;
    private final ProfissionalRepository profissionalRepository;
    private final AuditoriaAtendimentoService auditoriaService;

    /** Cria o atendimento a partir de um horário publicado e bloqueia esse horário na mesma transação. */
    @Transactional
    public Atendimento criar(AtendimentoRequest req) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ALUNO".equals(sessao.perfil())) {
            throw erro(HttpStatus.FORBIDDEN, "Somente um aluno pode realizar um novo agendamento.");
        }
        if (req.areaId() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "A área de atendimento é obrigatória.");
        }
        if (req.horarioId() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione um horário publicado pelo profissional.");
        }

        AreaServico area = areaRepository.findById(req.areaId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Área de serviço não encontrada."));
        entityManager.lock(area, LockModeType.PESSIMISTIC_WRITE);

        HorarioDisponivel horario = buscarHorarioParaAtualizacao(req.horarioId());
        validarHorarioDisponivel(horario, req.profissionalId());
        var profissional = profissionalRepository.findById(horario.getProfissionalId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Profissional não encontrado."));
        entityManager.lock(profissional, LockModeType.PESSIMISTIC_WRITE);
        Long areaProfissional = profissional.getAreaId();
        if (!Objects.equals(area.getId(), areaProfissional)) {
            throw erro(HttpStatus.BAD_REQUEST, "O profissional não atende na área selecionada.");
        }

        Long alunoId = sessao.userId();
        boolean jaPossuiAtendimentoNaArea = repository.existsByAlunoIdAndAreaIdAndStatusIn(
                alunoId, req.areaId(), STATUS_EM_ABERTO);
        if (jaPossuiAtendimentoNaArea) {
            throw erro(HttpStatus.CONFLICT,
                    "Você já possui um atendimento agendado nesta área. Cancele ou finalize o anterior antes de agendar novamente.");
        }

        validarLimiteDeFaltas(alunoId);

        IntervaloHorario intervalo = obterIntervalo(horario);
        LocalDateTime agora = agora();
        if (!intervalo.inicio().isAfter(agora)) {
            throw erro(HttpStatus.BAD_REQUEST, "Não é possível agendar um horário que já começou ou está no passado.");
        }

        Atendimento atendimento = new Atendimento();
        atendimento.setAlunoId(alunoId);
        atendimento.setProfissionalId(horario.getProfissionalId());
        atendimento.setAreaId(req.areaId());
        atendimento.setHorarioId(horario.getId());
        atendimento.setDataHoraInicio(intervalo.inicio());
        atendimento.setDataHoraFim(intervalo.fim());
        atendimento.setModalidade(req.modalidade());
        atendimento.setLocal(req.local());
        atendimento.setStatus(AGENDADO);

        horario.setDisponivel(false);
        horarioRepository.save(horario);

        Atendimento salvo = repository.save(atendimento);
        auditoriaService.registrar(salvo, "CRIAR");
        notificacaoService.notificarAgendamentoCriado(salvo);

        log.info("[RF08/RGN10] Atendimento {} criado: aluno={}, area={}, horario={}, inicio={}",
                salvo.getId(), alunoId, req.areaId(), horario.getId(), intervalo.inicio());
        return salvo;
    }

    public List<Atendimento> listar() {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ALUNO".equals(sessao.perfil())) {
            return auditarLista(repository.findByAlunoId(sessao.userId()), "CONSULTAR");
        }
        if ("PROFISSIONAL".equals(sessao.perfil())) {
            return auditarLista(repository.findByProfissionalId(sessao.userId()), "CONSULTAR");
        }
        if ("ADMIN".equals(sessao.perfil())) {
            return auditarLista(repository.findAll(), "CONSULTAR_AGENDA");
        }
        throw erro(HttpStatus.FORBIDDEN, "Perfil sem acesso aos atendimentos.");
    }

    public Atendimento buscar(Long id) {
        Atendimento atendimento = buscarEntidade(id);
        aplicarOwnership(atendimento);
        auditoriaService.registrar(atendimento, "CONSULTAR");
        return atendimento;
    }

    public List<Atendimento> listarPorAluno(Long alunoId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ALUNO".equals(sessao.perfil()) && !Objects.equals(sessao.userId(), alunoId)) {
            throw erro(HttpStatus.FORBIDDEN, "Você não pode consultar o histórico de outro aluno.");
        }
        if ("PROFISSIONAL".equals(sessao.perfil())) {
            if (!repository.existsByAlunoIdAndProfissionalId(alunoId, sessao.userId())) {
                throw erro(HttpStatus.FORBIDDEN,
                        "Você não tem atendimento vinculado a este aluno.");
            }
            return auditarLista(repository.findByAlunoId(alunoId).stream()
                    .filter(atendimento -> Objects.equals(atendimento.getProfissionalId(), sessao.userId()))
                    .toList(), "CONSULTAR_HISTORICO");
        }
        if (!"ALUNO".equals(sessao.perfil()) && !"ADMIN".equals(sessao.perfil())) {
            throw erro(HttpStatus.FORBIDDEN, "Perfil sem acesso ao histórico.");
        }
        return auditarLista(repository.findByAlunoId(alunoId), "CONSULTAR_HISTORICO");
    }

    public List<Atendimento> listarPorProfissional(Long profissionalId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())
                && (!"PROFISSIONAL".equals(sessao.perfil())
                || !Objects.equals(sessao.userId(), profissionalId))) {
            throw erro(HttpStatus.FORBIDDEN, "Você não pode consultar a agenda deste profissional.");
        }
        return auditarLista(repository.findByProfissionalId(profissionalId), "CONSULTAR_AGENDA");
    }

    /** O PUT legado permanece disponível, mas não pode alterar horário ou identidade e contornar a remarcação. */
    @Transactional
    public Atendimento atualizar(Long id, AtendimentoRequest req) {
        Atendimento atendimento = buscar(id);
        validarStatusEmAberto(atendimento);

        if (!Objects.equals(req.dataHoraInicio(), atendimento.getDataHoraInicio())
                || !Objects.equals(req.dataHoraFim(), atendimento.getDataHoraFim())) {
            throw erro(HttpStatus.BAD_REQUEST,
                    "Alterações de data e horário devem ser feitas pela operação de remarcação, respeitando 24 horas de antecedência.");
        }

        atendimento.setModalidade(req.modalidade());
        atendimento.setLocal(req.local());
        Atendimento salvo = repository.save(atendimento);
        auditoriaService.registrar(salvo, "ALTERAR");
        return salvo;
    }

    /**
     * Cancelamento:
     *  - Aluno, <24h de antecedência -> FALTA (RGN04; o slot do profissional foi consumido).
     *  - Aluno, >=24h                 -> CANCELADO e libera o horário.
     *  - Profissional (qualquer prazo) -> CANCELADO e libera o horário (o profissional é quem está devolvendo o slot).
     */
    @Transactional
    public Atendimento cancelar(Long id) {
        Atendimento atendimento = buscarEntidade(id);
        exigirAlunoDonoOuProfissionalResponsavel(atendimento);
        validarStatusEmAberto(atendimento);

        SessionContext.SessionInfo sessao = exigirSessao();
        boolean alunoDono = "ALUNO".equals(sessao.perfil());
        LocalDateTime agora = agora();

        if (alunoDono && atendimento.getDataHoraInicio().isBefore(agora.plusHours(24))) {
            atendimento.setStatus(FALTA);
            log.info("[RF18/RGN04] Aluno {} cancelou atendimento {} com menos de 24h -> FALTA",
                    sessao.userId(), id);
        } else {
            atendimento.setStatus(CANCELADO);
            liberarHorario(atendimento);
            log.info("[RF18] Atendimento {} cancelado por {} -> CANCELADO (horário liberado)",
                    id, sessao.perfil());
        }

        Atendimento salvo = repository.save(atendimento);
        auditoriaService.registrar(salvo, "CANCELAR");
        notificacaoService.notificarCancelamentoAtendimento(salvo);
        return salvo;
    }

    /** Remarcação conforme RF23: somente o aluno dono, com no mínimo 24h de antecedência. */
    @Transactional
    public Atendimento remarcar(Long id, RemarcarAtendimentoRequest req) {
        Atendimento atendimento = buscarEntidade(id);
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ALUNO".equals(sessao.perfil()) || !sessao.userId().equals(atendimento.getAlunoId())) {
            throw erro(HttpStatus.FORBIDDEN, "Somente o aluno responsável pode remarcar este atendimento.");
        }
        validarStatusEmAberto(atendimento);

        LocalDateTime agora = agora();
        if (atendimento.getDataHoraInicio().isBefore(agora.plusHours(24))) {
            throw erro(HttpStatus.BAD_REQUEST,
                    "A remarcação deve ser realizada com pelo menos 24 horas de antecedência.");
        }
        if (req == null || req.horarioId() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione o novo horário para remarcar o atendimento.");
        }
        if (Objects.equals(atendimento.getHorarioId(), req.horarioId())) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione um horário diferente do atual.");
        }

        HorarioDisponivel novoHorario = buscarHorarioParaAtualizacao(req.horarioId());
        validarHorarioDisponivel(novoHorario, atendimento.getProfissionalId());
        IntervaloHorario novoIntervalo = obterIntervalo(novoHorario);
        if (!novoIntervalo.inicio().isAfter(agora)) {
            throw erro(HttpStatus.BAD_REQUEST, "O novo horário precisa estar no futuro.");
        }

        liberarHorario(atendimento);
        novoHorario.setDisponivel(false);
        horarioRepository.save(novoHorario);

        atendimento.setHorarioId(novoHorario.getId());
        atendimento.setDataHoraInicio(novoIntervalo.inicio());
        atendimento.setDataHoraFim(novoIntervalo.fim());

        Atendimento salvo = repository.save(atendimento);
        auditoriaService.registrar(salvo, "REMARCAR");
        notificacaoService.notificarRemarcacaoAtendimento(salvo);

        log.info("[RF23] Atendimento {} remarcado: novoHorario={}, novoInicio={}",
                id, novoHorario.getId(), novoIntervalo.inicio());
        return salvo;
    }

    /** Finalização permitida somente ao profissional responsável e após o início agendado. */
    @Transactional
    public Atendimento finalizar(Long id) {
        Atendimento atendimento = buscarEntidade(id);
        exigirProfissionalResponsavel(atendimento);
        validarStatusEmAberto(atendimento);
        if (agora().isBefore(atendimento.getDataHoraInicio())) {
            throw erro(HttpStatus.BAD_REQUEST, "O atendimento só pode ser finalizado após o horário de início.");
        }
        atendimento.setStatus(REALIZADO);
        Atendimento salvo = repository.save(atendimento);
        auditoriaService.registrar(salvo, "FINALIZAR");
        return salvo;
    }

    /** Registro manual de falta pelo profissional responsável, após o início do atendimento. */
    @Transactional
    public Atendimento registrarFalta(Long id) {
        Atendimento atendimento = buscarEntidade(id);
        exigirProfissionalResponsavel(atendimento);
        validarStatusEmAberto(atendimento);
        if (agora().isBefore(atendimento.getDataHoraInicio())) {
            throw erro(HttpStatus.BAD_REQUEST, "A falta só pode ser registrada após o início do horário agendado.");
        }
        atendimento.setStatus(FALTA);
        Atendimento salvo = repository.save(atendimento);
        auditoriaService.registrar(salvo, "REGISTRAR_FALTA");
        return salvo;
    }

    /** Remoção física é uma operação administrativa excepcional; o fluxo normal é cancelar. */
    @Transactional
    public void excluir(Long id) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())) {
            throw erro(HttpStatus.FORBIDDEN, "Somente um administrador pode excluir um atendimento.");
        }
        buscarEntidade(id);
        throw erro(HttpStatus.CONFLICT,
                "Atendimentos devem ser preservados no histórico. Use cancelamento em vez de exclusão.");
    }

    /** Executado de hora em hora: registra FALTA após mais de 24h do início do atendimento. */
    @Scheduled(cron = "0 0 * * * *", zone = "America/Fortaleza")
    @Transactional
    public void marcarFaltasAutomaticamente() {
        LocalDateTime limite = agora().minusHours(24);
        List<Atendimento> atrasados = repository.findByStatusInAndDataHoraFimBefore(
                STATUS_EM_ABERTO, limite);
        if (atrasados.isEmpty()) {
            return;
        }
        atrasados.forEach(atendimento -> atendimento.setStatus(FALTA));
        repository.saveAll(atrasados);
        auditoriaService.registrarTodos(atrasados, "FALTA_AUTOMATICA");
        log.info("[RF32] {} atendimento(s) marcados como FALTA automaticamente.", atrasados.size());
    }

    private void validarLimiteDeFaltas(Long alunoId) {
        LocalDate hoje = LocalDate.now(FUSO_PAP);
        LocalDate inicioSemestre;
        LocalDate fimSemestre;
        if (hoje.getMonthValue() <= 6) {
            inicioSemestre = LocalDate.of(hoje.getYear(), 1, 1);
            fimSemestre = LocalDate.of(hoje.getYear(), 6, 30);
        } else {
            inicioSemestre = LocalDate.of(hoje.getYear(), 7, 1);
            fimSemestre = LocalDate.of(hoje.getYear(), 12, 31);
        }

        long faltas = repository.countByAlunoIdAndStatusAndDataHoraInicioBetween(
                alunoId, FALTA, inicioSemestre.atStartOfDay(), fimSemestre.atTime(LocalTime.MAX));
        if (faltas >= 3) {
            log.info("[RGN05] Bloqueio de agendamento para aluno {}: {} faltas no semestre", alunoId, faltas);
            throw erro(HttpStatus.FORBIDDEN, MENSAGEM_BLOQUEIO_FALTAS);
        }
    }

    private void validarHorarioDisponivel(HorarioDisponivel horario, Long profissionalEsperado) {
        if (!Boolean.TRUE.equals(horario.getDisponivel())) {
            throw erro(HttpStatus.CONFLICT, "Horário já reservado. Escolha outro.");
        }
        if (Boolean.TRUE.equals(horario.getBloqueado())) {
            throw erro(HttpStatus.BAD_REQUEST, "Horário bloqueado pelo profissional.");
        }
        if (profissionalEsperado == null
                || !Objects.equals(horario.getProfissionalId(), profissionalEsperado)) {
            throw erro(HttpStatus.BAD_REQUEST, "O horário selecionado não pertence ao profissional informado.");
        }
    }

    private IntervaloHorario obterIntervalo(HorarioDisponivel horario) {
        if (horario.getData() == null || horario.getHoraInicio() == null || horario.getHoraFim() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "O horário publicado está incompleto. Selecione outro horário.");
        }
        LocalDateTime inicio = LocalDateTime.of(horario.getData(), horario.getHoraInicio());
        LocalDateTime fim = LocalDateTime.of(horario.getData(), horario.getHoraFim());
        if (!fim.isAfter(inicio)) {
            throw erro(HttpStatus.BAD_REQUEST, "O horário publicado possui um intervalo inválido.");
        }
        return new IntervaloHorario(inicio, fim);
    }

    private HorarioDisponivel buscarHorarioParaAtualizacao(Long horarioId) {
        return horarioRepository.findByIdForUpdate(horarioId)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Horário não encontrado."));
    }

    private Atendimento buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Atendimento não encontrado."));
    }

    private void aplicarOwnership(Atendimento atendimento) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ADMIN".equals(sessao.perfil())) {
            return;
        }
        boolean alunoDono = "ALUNO".equals(sessao.perfil())
                && sessao.userId().equals(atendimento.getAlunoId());
        boolean profissionalResponsavel = "PROFISSIONAL".equals(sessao.perfil())
                && sessao.userId().equals(atendimento.getProfissionalId());
        if (!alunoDono && !profissionalResponsavel) {
            throw erro(HttpStatus.FORBIDDEN, "Você não tem acesso a este atendimento.");
        }
    }

    private void exigirAlunoDonoOuProfissionalResponsavel(Atendimento atendimento) {
        SessionContext.SessionInfo sessao = exigirSessao();
        boolean alunoDono = "ALUNO".equals(sessao.perfil())
                && sessao.userId().equals(atendimento.getAlunoId());
        boolean profissionalResponsavel = "PROFISSIONAL".equals(sessao.perfil())
                && sessao.userId().equals(atendimento.getProfissionalId());
        if (!alunoDono && !profissionalResponsavel) {
            throw erro(HttpStatus.FORBIDDEN,
                    "Somente o aluno responsável ou o profissional do atendimento pode cancelar.");
        }
    }

    private void exigirProfissionalResponsavel(Atendimento atendimento) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"PROFISSIONAL".equals(sessao.perfil())
                || !sessao.userId().equals(atendimento.getProfissionalId())) {
            throw erro(HttpStatus.FORBIDDEN, "Apenas o profissional responsável pode realizar esta operação.");
        }
    }

    private void validarStatusEmAberto(Atendimento atendimento) {
        if (!STATUS_EM_ABERTO.contains(atendimento.getStatus())) {
            throw erro(HttpStatus.CONFLICT,
                    "Somente atendimentos AGENDADO podem passar por esta operação.");
        }
    }

    private SessionContext.SessionInfo exigirSessao() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw erro(HttpStatus.UNAUTHORIZED, "Sua sessão expirou. Entre novamente no PAP Online.");
        }
        return sessao;
    }

    private void liberarHorario(Atendimento atendimento) {
        if (atendimento.getHorarioId() == null) {
            return;
        }
        horarioRepository.findByIdForUpdate(atendimento.getHorarioId()).ifPresent(horario -> {
            horario.setDisponivel(true);
            horarioRepository.save(horario);
        });
    }

    private List<Atendimento> auditarLista(List<Atendimento> atendimentos, String acao) {
        auditoriaService.registrarTodos(atendimentos, acao);
        return atendimentos;
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(FUSO_PAP);
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }

    private record IntervaloHorario(LocalDateTime inicio, LocalDateTime fim) {
    }
}