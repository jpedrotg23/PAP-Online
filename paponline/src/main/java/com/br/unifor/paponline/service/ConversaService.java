package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.entidades.Conversa;
import com.br.unifor.paponline.entidades.Mensagem;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.repository.ConversaRepository;
import com.br.unifor.paponline.repository.MensagemRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
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

    public Conversa obterOuCriar(Long alunoId, Long profissionalId) {
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
        return conversaRepository.findByAlunoId(alunoId);
    }

    public List<Conversa> listarPorProfissional(Long profissionalId) {
        // Devolve as conversas onde o usuário é o profissional responsável
        // — inclui tanto aluno↔prof quanto prof↔coordenação.
        return conversaRepository.findByProfissionalId(profissionalId);
    }

    public List<Mensagem> listarMensagens(Long conversaId) {
        return mensagemRepository.findByConversaIdOrderByEnviadaEmAsc(conversaId);
    }

    @Transactional
    public Mensagem enviar(Long conversaId, Long remetenteId, String conteudo) {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada.");
        }
        // Sempre usa o id da sessão — não confia no body
        Long efetivoRemetente = sessao.userId();

        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada"));

        // Verifica se o remetente participa da conversa
        boolean participa = Objects.equals(efetivoRemetente, conversa.getAlunoId())
                || Objects.equals(efetivoRemetente, conversa.getProfissionalId())
                || Objects.equals(efetivoRemetente, conversa.getAdminId());
        if (!participa) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não participa desta conversa.");
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

    public void marcarComoLida(Long mensagemId) {
        Mensagem m = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada"));
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
}