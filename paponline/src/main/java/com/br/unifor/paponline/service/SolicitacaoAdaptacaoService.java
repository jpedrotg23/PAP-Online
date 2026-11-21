package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import com.br.unifor.paponline.repository.AreaServicoRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SolicitacaoAdaptacaoService {

    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");

    private static final String ENVIADA = "ENVIADA";
    private static final String EM_ANALISE = "EM_ANALISE";
    private static final String DEFERIDA = "DEFERIDA";
    private static final String INDEFERIDA = "INDEFERIDA";
    private static final String PENDENTE_DOCUMENTACAO = "PENDENTE_DOCUMENTACAO";

    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "PROVA", "PRAZO", "MATERIAL", "DISPENSA", "LOCAL_PROVA", "OUTRO"
    );

    private static final Set<String> STATUS_VALIDOS = Set.of(
            ENVIADA, EM_ANALISE, DEFERIDA, INDEFERIDA, PENDENTE_DOCUMENTACAO
    );

    private final SolicitacaoAdaptacaoRepository repository;
    private final NotificacaoService notificacaoService;
    private final AreaServicoRepository areaRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional
    public SolicitacaoAdaptacao criar(SolicitacaoAdaptacao dados) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ALUNO".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas alunos podem criar solicitações de adaptação.");
        }

        if (dados == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe os dados da solicitação.");
        }

        String tipo = validarTipo(dados.getTipo());
        String titulo = validarTextoObrigatorio(dados.getTitulo(), "Informe o título da solicitação.");
        String justificativa = validarTextoObrigatorio(dados.getJustificativa(), "Informe a justificativa da solicitação.");

        LocalDateTime agora = LocalDateTime.now(FUSO_PAP);

        // Ignora alunoId, status, parecer, profissionalId e datas vindos do cliente
        SolicitacaoAdaptacao nova = new SolicitacaoAdaptacao();
        nova.setProtocolo(gerarProtocolo());
        nova.setAlunoId(sessao.userId());
        if (dados.getAreaId() != null && !areaRepository.existsById(dados.getAreaId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Área de serviço inválida.");
        }
        nova.setAreaId(dados.getAreaId());
        nova.setProfissionalId(null);
        nova.setTipo(tipo);
        nova.setTitulo(titulo);
        nova.setJustificativa(justificativa);
        nova.setStatus(ENVIADA);
        nova.setParecer(null);
        nova.setDataEnvio(agora);
        nova.setPrazoParecer(calcularPrazoParecer(agora.toLocalDate(), 5));
        nova.setDataParecer(null);

        SolicitacaoAdaptacao salva = repository.save(nova);
        notificacaoService.notificarNovaSolicitacao(salva);
        return salva;
    }

    public List<SolicitacaoAdaptacao> listar() {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ALUNO".equals(sessao.perfil())) {
            return repository.findByAlunoId(sessao.userId());
        }
        if ("PROFISSIONAL".equals(sessao.perfil())) {
            return repository.findByProfissionalId(sessao.userId());
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Administradores não podem consultar o conteúdo das solicitações.");
    }

    public SolicitacaoAdaptacao buscar(Long id) {
        SessionContext.SessionInfo sessao = exigirSessao();
        SolicitacaoAdaptacao s = buscarEntidade(id);
        if ("ALUNO".equals(sessao.perfil()) && !Objects.equals(s.getAlunoId(), sessao.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para visualizar esta solicitação.");
        }
        if ("PROFISSIONAL".equals(sessao.perfil())
                && !Objects.equals(s.getProfissionalId(), sessao.userId())
                && (s.getAreaId() == null
                || !Objects.equals(s.getAreaId(), areaDoProfissional(sessao.userId())))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não está autorizado a consultar esta solicitação.");
        }
        if ("ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administradores não podem consultar o conteúdo das solicitações.");
        }
        return s;
    }

    @Transactional
    public SolicitacaoAdaptacao atualizar(Long id, SolicitacaoAdaptacao dados) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ALUNO".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas alunos podem editar solicitações.");
        }

        SolicitacaoAdaptacao s = buscarEntidade(id);
        Long areaProfissional = areaDoProfissional(sessao.userId());
        if (s.getProfissionalId() != null && !Objects.equals(s.getProfissionalId(), sessao.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A solicitação está atribuída a outro profissional.");
        }
        if (s.getAreaId() == null || !Objects.equals(s.getAreaId(), areaProfissional)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A solicitação não está vinculada à sua área de atuação.");
        }
        if (!Objects.equals(s.getAlunoId(), sessao.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para editar esta solicitação.");
        }

        if (!ENVIADA.equals(s.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A solicitação só pode ser editada enquanto estiver com status ENVIADA.");
        }

        if (dados == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe os dados da solicitação.");
        }

        s.setTipo(validarTipo(dados.getTipo()));
        s.setTitulo(validarTextoObrigatorio(dados.getTitulo(), "Informe o título da solicitação."));
        s.setJustificativa(validarTextoObrigatorio(dados.getJustificativa(), "Informe a justificativa da solicitação."));

        return repository.save(s);
    }

    @Transactional
    public SolicitacaoAdaptacao analisar(Long id, Map<String, Object> body) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"PROFISSIONAL".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas profissionais podem analisar solicitações.");
        }

        SolicitacaoAdaptacao s = buscarEntidade(id);

        String statusBruto = body != null && body.get("status") != null
                ? String.valueOf(body.get("status")).trim().toUpperCase()
                : "";
        if (statusBruto.isBlank() || !STATUS_VALIDOS.contains(statusBruto)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um status válido para a solicitação.");
        }

        validarTransicaoStatus(s.getStatus(), statusBruto);

        String parecer = body != null && body.get("parecer") != null
                ? String.valueOf(body.get("parecer")).trim()
                : "";

        boolean exigeParecer = DEFERIDA.equals(statusBruto)
                || INDEFERIDA.equals(statusBruto)
                || PENDENTE_DOCUMENTACAO.equals(statusBruto);

        if (exigeParecer && parecer.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O parecer é obrigatório para DEFERIDA, INDEFERIDA e PENDENTE_DOCUMENTACAO.");
        }

        // profissionalId vem sempre da sessão
        s.setProfissionalId(sessao.userId());
        s.setStatus(statusBruto);

        if (!parecer.isBlank()) {
            s.setParecer(parecer);
        }
        if (exigeParecer) {
            s.setDataParecer(LocalDateTime.now(FUSO_PAP));
        }

        SolicitacaoAdaptacao atualizada = repository.save(s);
        notificacaoService.notificarMudancaStatusSolicitacao(atualizada);
        return atualizada;
    }

    @Transactional
    public void excluir(Long id) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas administradores podem excluir solicitações.");
        }
        SolicitacaoAdaptacao s = buscarEntidade(id);
        repository.delete(s);
    }

    public List<SolicitacaoAdaptacao> listarPorAluno(Long alunoId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ALUNO".equals(sessao.perfil()) && !Objects.equals(sessao.userId(), alunoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você só pode consultar suas próprias solicitações.");
        }
        if ("PROFISSIONAL".equals(sessao.perfil())) {
            return repository.findByAlunoId(alunoId).stream()
                    .filter(s -> Objects.equals(s.getProfissionalId(), sessao.userId())
                            || (s.getAreaId() != null
                            && Objects.equals(s.getAreaId(), areaDoProfissional(sessao.userId()))))
                    .toList();
        }
        if (!"ALUNO".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administradores não podem consultar o conteúdo das solicitações.");
        }
        return repository.findByAlunoId(alunoId);
    }

    public List<SolicitacaoAdaptacao> listarPorStatus(String status) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"PROFISSIONAL".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente profissionais podem consultar solicitações por status.");
        }
        return repository.findByStatus(status).stream()
                .filter(s -> Objects.equals(s.getProfissionalId(), sessao.userId())
                        || (s.getAreaId() != null
                        && Objects.equals(s.getAreaId(), areaDoProfissional(sessao.userId()))))
                .toList();
    }

    private SolicitacaoAdaptacao buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
    }

    private void validarTransicaoStatus(String atual, String destino) {
        boolean valida = switch (atual) {
            case ENVIADA -> EM_ANALISE.equals(destino);
            case EM_ANALISE -> DEFERIDA.equals(destino)
                    || INDEFERIDA.equals(destino)
                    || PENDENTE_DOCUMENTACAO.equals(destino);
            case PENDENTE_DOCUMENTACAO -> EM_ANALISE.equals(destino);
            default -> false;
        };

        if (!valida) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transição de status não permitida: " + atual + " -> " + destino + ".");
        }
    }

    private LocalDate calcularPrazoParecer(LocalDate dataInicial, int diasUteis) {
        LocalDate data = dataInicial;
        int adicionados = 0;
        while (adicionados < diasUteis) {
            data = data.plusDays(1);
            DayOfWeek diaSemana = data.getDayOfWeek();
            if (diaSemana != DayOfWeek.SATURDAY && diaSemana != DayOfWeek.SUNDAY) {
                adicionados++;
            }
        }
        return data;
    }

    private String gerarProtocolo() {
        int ano = LocalDate.now(FUSO_PAP).getYear();
        String prefixo = "PAP-" + ano + "-";
        List<SolicitacaoAdaptacao> doAno = repository.findByProtocoloStartingWith(prefixo);
        int proximo = 1;
        for (SolicitacaoAdaptacao s : doAno) {
            String seq = s.getProtocolo().substring(prefixo.length());
            try {
                int n = Integer.parseInt(seq);
                if (n >= proximo) {
                    proximo = n + 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("%s%04d", prefixo, proximo);
    }

    private String validarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o tipo da solicitação.");
        }
        String normalizado = tipo.trim().toUpperCase();
        if (!TIPOS_VALIDOS.contains(normalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de solicitação inválido: " + tipo);
        }
        return normalizado;
    }

    private String validarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }
        return valor.trim();
    }

    private SessionContext.SessionInfo exigirSessao() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Sua sessão expirou. Entre novamente no PAP Online.");
        }
        return sessao;
    }

    private Long areaDoProfissional(Long profissionalId) {
        return profissionalRepository.findById(profissionalId)
                .map(profissional -> profissional.getAreaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Profissional não encontrado ou sem área de atuação."));
    }
}