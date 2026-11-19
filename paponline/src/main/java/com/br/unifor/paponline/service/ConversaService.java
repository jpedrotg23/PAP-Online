package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Conversa;
import com.br.unifor.paponline.entidades.Mensagem;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.ConversaRepository;
import com.br.unifor.paponline.repository.MensagemRepository;
import com.br.unifor.paponline.repository.ProfissionalRepository;
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
    private final AtendimentoRepository atendimentoRepository;
    private final AlunoRepository alunoRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional
    public Conversa obterOuCriar(Long alunoIdParam, Long profissionalIdParam) {
        SessionContext.SessionInfo sessao = exigirSessao();

        if ("ADMIN".equals(sessao.perfil())) {
            return obterOuCriarAdmin(sessao, alunoIdParam, profissionalIdParam);
        }

        Long alunoId = alunoIdParam;
        Long profissionalId = profissionalIdParam;

        if ("ALUNO".equals(sessao.perfil())) {
            if (alunoId != null && !Objects.equals(alunoId, sessao.userId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você só pode iniciar conversas em seu próprio nome.");
            }
            alunoId = sessao.userId();
        } else if ("PROFISSIONAL".equals(sessao.perfil())) {
            if (profissionalId != null && !Objects.equals(profissionalId, sessao.userId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Você só pode iniciar conversas em seu próprio nome.");
            }
            profissionalId = sessao.userId();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Perfil sem permissão para iniciar conversas.");
        }

        if (alunoId == null || profissionalId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe o aluno e o profissional para iniciar a conversa.");
        }

        if (!alunoRepository.existsById(alunoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado.");
        }
        if (!profissionalRepository.existsById(profissionalId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado.");
        }

        boolean possuemAtendimento = atendimentoRepository.existsByAlunoIdAndProfissionalId(alunoId, profissionalId);
        if (!possuemAtendimento) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Só é possível criar conversa entre um aluno e um profissional que tenham ou tenham tido atendimento entre si.");
        }

        Long finalAlunoId = alunoId;
        Long finalProfissionalId = profissionalId;

        return conversaRepository.findByAlunoIdAndProfissionalId(finalAlunoId, finalProfissionalId)
                .orElseGet(() -> {
                    Conversa c = new Conversa();
                    c.setAlunoId(finalAlunoId);
                    c.setProfissionalId(finalProfissionalId);
                    return conversaRepository.save(c);
                });
    }

    private Conversa obterOuCriarAdmin(SessionContext.SessionInfo sessao,
                                       Long alunoIdParam, Long profissionalIdParam) {
        boolean temAluno = alunoIdParam != null;
        boolean temProf = profissionalIdParam != null;

        if (temAluno == temProf) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe exatamente um destinatário: aluno OU profissional.");
        }

        Long adminId = sessao.userId();

        if (temAluno) {
            if (!alunoRepository.existsById(alunoIdParam)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado.");
            }
            Long finalAluno = alunoIdParam;
            return conversaRepository.findByAlunoIdAndAdminId(finalAluno, adminId)
                    .orElseGet(() -> {
                        Conversa c = new Conversa();
                        c.setAlunoId(finalAluno);
                        c.setAdminId(adminId);
                        return conversaRepository.save(c);
                    });
        }

        if (!profissionalRepository.existsById(profissionalIdParam)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado.");
        }
        Long finalProf = profissionalIdParam;
        return conversaRepository.findByProfissionalIdAndAdminId(finalProf, adminId)
                .orElseGet(() -> {
                    Conversa c = new Conversa();
                    c.setProfissionalId(finalProf);
                    c.setAdminId(adminId);
                    return conversaRepository.save(c);
                });
    }

    public List<Conversa> listarPorAluno(Long alunoId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())
                && (!"ALUNO".equals(sessao.perfil()) || !Objects.equals(sessao.userId(), alunoId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar estas conversas.");
        }
        return conversaRepository.findByAlunoId(alunoId);
    }

    public List<Conversa> listarPorProfissional(Long profissionalId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())
                && (!"PROFISSIONAL".equals(sessao.perfil()) || !Objects.equals(sessao.userId(), profissionalId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar estas conversas.");
        }
        return conversaRepository.findByProfissionalId(profissionalId);
    }

    public List<Conversa> listarPorAdmin(Long adminId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil()) || !Objects.equals(sessao.userId(), adminId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar estas conversas.");
        }
        return conversaRepository.findByAdminId(adminId);
    }

    public List<Mensagem> listarMensagens(Long conversaId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        validarAcessoConversa(conversaId, sessao);
        return mensagemRepository.findByConversaIdOrderByEnviadaEmAsc(conversaId);
    }

    @Transactional
    public Mensagem enviar(Long conversaId, String conteudo) {
        SessionContext.SessionInfo sessao = exigirSessao();
        Conversa conversa = validarAcessoConversa(conversaId, sessao);

        boolean participa = Objects.equals(conversa.getAlunoId(), sessao.userId())
                || Objects.equals(conversa.getProfissionalId(), sessao.userId())
                || Objects.equals(conversa.getAdminId(), sessao.userId());

        if (!participa) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas os participantes da conversa podem enviar mensagens.");
        }

        if (conteudo == null || conteudo.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem não pode estar vazia.");
        }

        Mensagem m = new Mensagem();
        m.setConversaId(conversaId);
        m.setRemetenteId(sessao.userId());
        m.setConteudo(conteudo.trim());
        m.setEnviadaEm(LocalDateTime.now());
        Mensagem salva = mensagemRepository.save(m);

        conversa.setAtualizadoEm(LocalDateTime.now());
        conversaRepository.save(conversa);

        return salva;
    }

    @Transactional
    public void marcarComoLida(Long mensagemId) {
        SessionContext.SessionInfo sessao = exigirSessao();
        Mensagem m = mensagemRepository.findById(mensagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada"));
        validarAcessoConversa(m.getConversaId(), sessao);

        if (!Objects.equals(m.getRemetenteId(), sessao.userId())) {
            m.setLida(true);
            mensagemRepository.save(m);
        }
    }

    private Conversa validarAcessoConversa(Long conversaId, SessionContext.SessionInfo sessao) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada."));

        boolean participa = Objects.equals(conversa.getAlunoId(), sessao.userId())
                || Objects.equals(conversa.getProfissionalId(), sessao.userId())
                || Objects.equals(conversa.getAdminId(), sessao.userId());

        if (!participa && !"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar esta conversa.");
        }
        return conversa;
    }

    private SessionContext.SessionInfo exigirSessao() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Sua sessão expirou. Entre novamente no PAP Online.");
        }
        return sessao;
    }
}