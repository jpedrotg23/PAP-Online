package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.entidades.Conversa;
import com.br.unifor.paponline.entidades.Mensagem;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.repository.ConversaRepository;
import com.br.unifor.paponline.repository.MensagemRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConversaService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final AtendimentoRepository atendimentoRepository;

    @Transactional
    public Conversa obterOuCriar(Long alunoId, Long profissionalId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        boolean participante = ("ALUNO".equals(sessao.perfil()) && Objects.equals(sessao.userId(), alunoId))
                || ("PROFISSIONAL".equals(sessao.perfil())
                && Objects.equals(sessao.userId(), profissionalId));
        if (!participante) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente um dos participantes pode abrir esta conversa.");
        }
        if (alunoId == null || profissionalId == null
                || !atendimentoRepository.existsByAlunoIdAndProfissionalId(alunoId, profissionalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A conversa exige atendimento atual ou anterior entre o aluno e o profissional.");
        }
        return conversaRepository.findByAlunoIdAndProfissionalId(alunoId, profissionalId)
                .orElseGet(() -> {
                    Conversa c = new Conversa();
                    c.setAlunoId(alunoId);
                    c.setProfissionalId(profissionalId);
                    return conversaRepository.save(c);
                });
    }

    /**
     * Cria (ou reabre) a conversa entre o profissional logado e a coordenação.
     * Se for chamado por um ADMIN com profissionalId no parâmetro, também funciona
     * (para uso futuro no admin-mensagens.html).
     */
    @Transactional
    public Conversa criarComCoordenacao(Long profissionalIdParam) {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada.");
        }

        Long adminId;
        Long profissionalId;

        if ("PROFISSIONAL".equals(sessao.perfil())) {
            profissionalId = sessao.userId();
            adminId = encontrarAdminAtivoId();
        } else if ("ADMIN".equals(sessao.perfil())) {
            adminId = sessao.userId();
            if (profissionalIdParam == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Informe o profissionalId ao criar conversa como coordenação.");
            }
            profissionalId = profissionalIdParam;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente profissionais ou coordenação podem abrir este tipo de conversa.");
        }

        Long finalAdmin = adminId;
        Long finalProf = profissionalId;
        return conversaRepository.findByAdminIdAndProfissionalId(adminId, profissionalId)
                .orElseGet(() -> {
                    Conversa c = new Conversa();
                    c.setAdminId(finalAdmin);
                    c.setProfissionalId(finalProf);
                    return conversaRepository.save(c);
                });
    }

    public List<Conversa> listarPorAluno(Long alunoId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ALUNO".equals(sessao.perfil())) {
            if (!Objects.equals(sessao.userId(), alunoId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode consultar suas conversas.");
            }
            return conversaRepository.findByAlunoId(alunoId);
        }
        if ("PROFISSIONAL".equals(sessao.perfil())) {
            return conversaRepository.findByAlunoId(alunoId).stream()
                    .filter(c -> Objects.equals(c.getProfissionalId(), sessao.userId()))
                    .toList();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Administradores não podem consultar conversas de alunos.");
    }

    public List<Conversa> listarPorProfissional(Long profissionalId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("PROFISSIONAL".equals(sessao.perfil())) {
            if (!Objects.equals(sessao.userId(), profissionalId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode consultar suas conversas.");
            }
        } else if (!"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem acesso a conversas profissionais.");
        }
        List<Conversa> conversas = conversaRepository.findByProfissionalId(profissionalId);
        return "ADMIN".equals(sessao.perfil())
                ? conversas.stream().filter(c -> c.getAdminId() != null).toList()
                : conversas;
    }

    public List<Mensagem> listarMensagens(Long conversaId) {
        exigirParticipante(buscarConversa(conversaId));
        return mensagemRepository.findByConversaIdOrderByEnviadaEmAsc(conversaId);
    }

    @Transactional
    public Mensagem enviar(Long conversaId, Long remetenteId, String conteudo) {
        SessionContext.SessionInfo sessao = exigirSessao();
        // Sempre usa o id da sessão — não confia no body
        Long efetivoRemetente = sessao.userId();

        Conversa conversa = buscarConversa(conversaId);

        // Verifica se o remetente participa da conversa
        boolean participa = Objects.equals(efetivoRemetente, conversa.getAlunoId())
                || Objects.equals(efetivoRemetente, conversa.getProfissionalId())
                || Objects.equals(efetivoRemetente, conversa.getAdminId());
        if (!participa) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não participa desta conversa.");
        }
        if (conteudo == null || conteudo.isBlank() || conteudo.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A mensagem deve conter entre 1 e 2000 caracteres.");
        }

        Mensagem m = new Mensagem();
        m.setConversaId(conversaId);
        m.setRemetenteId(efetivoRemetente);
        m.setConteudo(conteudo);
        m.setEnviadaEm(LocalDateTime.now());
        Mensagem salva = mensagemRepository.save(m);

        conversa.setAtualizadoEm(LocalDateTime.now());
        conversaRepository.save(conversa);

        return salva;
    }

    @Transactional
    public void marcarComoLida(Long mensagemId) {
        Mensagem m = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada"));
        exigirParticipante(buscarConversa(m.getConversaId()));
        if (Objects.equals(m.getRemetenteId(), exigirSessao().userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente o destinatário pode marcar esta mensagem como lida.");
        }
        m.setLida(true);
        mensagemRepository.save(m);
    }

    private Long encontrarAdminAtivoId() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u instanceof Administrador && Boolean.TRUE.equals(u.getAtivo()))
                .findFirst()
                .map(Usuario::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Nenhuma coordenação ativa no sistema."));
    }

    private Conversa buscarConversa(Long conversaId) {
        return conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada"));
    }

    private SessionContext.SessionInfo exigirSessao() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada.");
        }
        return sessao;
    }

    private void exigirParticipante(Conversa conversa) {
        SessionContext.SessionInfo sessao = exigirSessao();
        boolean participa = Objects.equals(sessao.userId(), conversa.getAlunoId())
                || Objects.equals(sessao.userId(), conversa.getProfissionalId())
                || Objects.equals(sessao.userId(), conversa.getAdminId());
        if (!participa) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não participa desta conversa.");
        }
    }
}