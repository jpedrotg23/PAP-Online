package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.entidades.AvisoInstitucional;
import com.br.unifor.paponline.entidades.Notificacao;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.NotificacaoRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String TIPO_LEMBRETE_24H = "LEMBRETE_24H";

    private final NotificacaoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AtendimentoRepository atendimentoRepository;

    @Transactional
    public Notificacao criar(Long usuarioId, String titulo, String mensagem) {
        return criar(usuarioId, titulo, mensagem, "GERAL", null);
    }

    public Notificacao criarManual(Long usuarioId, String titulo, String mensagem) {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || !"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente administradores podem criar notificações manualmente.");
        }
        return criar(usuarioId, titulo, mensagem, "GERAL", null);
    }

    @Transactional
    public Notificacao criar(Long usuarioId, String titulo, String mensagem, String tipo, Long referenciaId) {
        if (usuarioId == null) {
            return null;
        }
        Notificacao n = new Notificacao();
        n.setUsuarioId(usuarioId);
        n.setTitulo(titulo);
        n.setMensagem(mensagem);
        n.setTipo(tipo);
        n.setReferenciaId(referenciaId);
        n.setLida(false);
        n.setCriadaEm(LocalDateTime.now(FUSO_PAP));
        return repository.save(n);
    }

    public List<Notificacao> listarPorUsuario(Long usuarioId) {
        validarAcessoUsuario(usuarioId);
        return repository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId);
    }

    public long contarNaoLidas(Long usuarioId) {
        validarAcessoUsuario(usuarioId);
        return repository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public void marcarComoLida(Long id) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        validarAcessoUsuario(n.getUsuarioId());
        n.setLida(true);
        n.setLidaEm(LocalDateTime.now(FUSO_PAP));
        repository.save(n);
    }

    @Transactional
    public void excluir(Long id) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        validarAcessoUsuario(n.getUsuarioId());
        repository.delete(n);
    }

    @Transactional
    public void notificarAgendamentoCriado(Atendimento atendimento) {
        if (atendimento == null) {
            return;
        }
        String dataFmt = formatarDataHora(atendimento.getDataHoraInicio());
        criar(
                atendimento.getAlunoId(),
                "Agendamento confirmado",
                "Seu atendimento foi agendado para " + dataFmt + ".",
                "AGENDAMENTO",
                atendimento.getId()
        );
        criar(
                atendimento.getProfissionalId(),
                "Novo agendamento na agenda",
                "Um novo atendimento foi agendado para " + dataFmt + ".",
                "AGENDAMENTO",
                atendimento.getId()
        );
    }

    @Transactional
    public void notificarCancelamentoAtendimento(Atendimento atendimento) {
        if (atendimento == null) {
            return;
        }
        String dataFmt = formatarDataHora(atendimento.getDataHoraInicio());
        boolean virouFalta = "FALTA".equals(atendimento.getStatus());
        String detalhe = virouFalta
                ? "O atendimento de " + dataFmt + " foi cancelado com menos de 24h de antecedência e registrado como falta."
                : "O atendimento agendado para " + dataFmt + " foi cancelado.";

        criar(
                atendimento.getAlunoId(),
                "Atendimento cancelado",
                detalhe,
                "AGENDAMENTO",
                atendimento.getId()
        );
        criar(
                atendimento.getProfissionalId(),
                "Atendimento cancelado",
                detalhe,
                "AGENDAMENTO",
                atendimento.getId()
        );
    }

    @Transactional
    public void notificarRemarcacaoAtendimento(Atendimento atendimento) {
        if (atendimento == null) {
            return;
        }
        String dataFmt = formatarDataHora(atendimento.getDataHoraInicio());
        criar(
                atendimento.getAlunoId(),
                "Atendimento remarcado",
                "Seu atendimento foi remarcado para " + dataFmt + ".",
                "AGENDAMENTO",
                atendimento.getId()
        );
        criar(
                atendimento.getProfissionalId(),
                "Atendimento remarcado",
                "Um atendimento foi remarcado para " + dataFmt + ".",
                "AGENDAMENTO",
                atendimento.getId()
        );
    }

    @Transactional
    public void notificarNovaSolicitacao(SolicitacaoAdaptacao solicitacao) {
        if (solicitacao == null) {
            return;
        }
        List<Profissional> profissionaisAtivos = profissionalRepository.findByAtivoTrue().stream()
                .filter(profissional -> solicitacao.getAreaId() != null
                        && Objects.equals(profissional.getAreaId(), solicitacao.getAreaId()))
                .toList();
        for (Profissional profissional : profissionaisAtivos) {
            criar(
                    profissional.getId(),
                    "Nova solicitação de adaptação",
                    "Nova solicitação (" + solicitacao.getProtocolo() + "): " + solicitacao.getTitulo(),
                    "SOLICITACAO",
                    solicitacao.getId()
            );
        }
    }

    @Transactional
    public void notificarMudancaStatusSolicitacao(SolicitacaoAdaptacao solicitacao) {
        if (solicitacao == null) {
            return;
        }
        String statusRotulo = switch (solicitacao.getStatus()) {
            case "ENVIADA" -> "Enviada";
            case "EM_ANALISE" -> "Em análise";
            case "DEFERIDA" -> "Deferida";
            case "INDEFERIDA" -> "Indeferida";
            case "PENDENTE_DOCUMENTACAO" -> "Pendente de documentação";
            default -> solicitacao.getStatus();
        };

        criar(
                solicitacao.getAlunoId(),
                "Atualização na solicitação de adaptação",
                "Sua solicitação (" + solicitacao.getProtocolo() + ") mudou para o status: " + statusRotulo + ".",
                "SOLICITACAO",
                solicitacao.getId()
        );
    }

    @Transactional
    public void notificarAvisoPublicado(AvisoInstitucional aviso) {
        if (aviso == null) {
            return;
        }
        String publico = aviso.getPublicoAlvo() != null
                ? aviso.getPublicoAlvo().trim().toUpperCase()
                : "TODOS";
        String titulo = "Novo comunicado: " + aviso.getTitulo();
        String mensagem = resumir(aviso.getConteudo());

        if ("ALUNOS".equals(publico)) {
            for (Aluno aluno : alunoRepository.findByAtivoTrue()) {
                criar(aluno.getId(), titulo, mensagem, "AVISO", aviso.getId());
            }
        } else if ("PROFISSIONAIS".equals(publico)) {
            for (Profissional profissional : profissionalRepository.findByAtivoTrue()) {
                criar(profissional.getId(), titulo, mensagem, "AVISO", aviso.getId());
            }
        } else {
            for (Usuario usuario : usuarioRepository.findByAtivoTrue()) {
                criar(usuario.getId(), titulo, mensagem, "AVISO", aviso.getId());
            }
        }
    }

    /** Executado a cada 15 minutos: envia lembrete 24h antes do atendimento sem duplicar. */
    @Scheduled(fixedRate = 900000)
    @Transactional
    public void enviarLembretes24h() {
        LocalDateTime agora = LocalDateTime.now(FUSO_PAP);
        LocalDateTime em24Horas = agora.plusHours(24);

        List<Atendimento> proximos = atendimentoRepository.findByStatusAndDataHoraInicioBetween(
                "AGENDADO", agora, em24Horas
        );

        for (Atendimento atendimento : proximos) {
            String dataFmt = formatarDataHora(atendimento.getDataHoraInicio());

            if (atendimento.getAlunoId() != null
                    && !repository.existsByUsuarioIdAndTipoAndReferenciaId(
                    atendimento.getAlunoId(), TIPO_LEMBRETE_24H, atendimento.getId())) {
                criar(
                        atendimento.getAlunoId(),
                        "Lembrete de atendimento (24h)",
                        "Lembrete: você possui um atendimento agendado para " + dataFmt + ".",
                        TIPO_LEMBRETE_24H,
                        atendimento.getId()
                );
            }

            if (atendimento.getProfissionalId() != null
                    && !repository.existsByUsuarioIdAndTipoAndReferenciaId(
                    atendimento.getProfissionalId(), TIPO_LEMBRETE_24H, atendimento.getId())) {
                criar(
                        atendimento.getProfissionalId(),
                        "Lembrete de atendimento (24h)",
                        "Lembrete: você possui um atendimento agendado para " + dataFmt + ".",
                        TIPO_LEMBRETE_24H,
                        atendimento.getId()
                );
            }
        }
    }

    private void validarAcessoUsuario(Long usuarioId) {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sua sessão expirou. Entre novamente no PAP Online.");
        }
        if (!Objects.equals(sessao.userId(), usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar notificações de outro usuário.");
        }
    }

    private String formatarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            return "data a confirmar";
        }
        return dataHora.format(FORMATO_DATA_HORA);
    }

    private String resumir(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            return "Um novo comunicado institucional foi publicado no PAP Online.";
        }
        String limpo = conteudo.trim();
        return limpo.length() > 140 ? limpo.substring(0, 140) + "..." : limpo;
    }
}