package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.util.SenhaValidator;
import com.br.unifor.paponline.util.TelefoneValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Objects;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private static final ZoneId FUSO_PAP = ZoneId.of("America/Fortaleza");

    private final AlunoRepository repository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final AtendimentoRepository atendimentoRepository;

    @Value("${pap.consent.version:1.0}")
    private String versaoTermoConsentimento;

    public Aluno criar(Aluno aluno) {
        if (!Boolean.TRUE.equals(aluno.getConsentimentoAceito())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "É necessário aceitar o Termo de consentimento para criar a conta.");
        }
        if (repository.existsByMatricula(aluno.getMatricula())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um aluno com esta matrícula");
        }
        aluno.setEmail(normalizarEmail(aluno.getEmail()));
        if (usuarioRepository.existsByEmailIgnoreCase(aluno.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        validarTelefone(aluno.getTelefone());
        aluno.setConsentimentoVersao(versaoTermoConsentimento);
        aluno.setConsentimentoEm(LocalDateTime.now(FUSO_PAP));
        try {
            SenhaValidator.exigirValida(aluno.getSenha());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        aluno.setSenha(passwordEncoder.encode(aluno.getSenha()));
        return repository.save(aluno);
    }

    public List<Aluno> listar() {
        exigirAdministrador();
        return repository.findAll();
    }

    public Aluno buscar(Long id) {
        Aluno aluno = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
        SessionContext.SessionInfo sessao = exigirSessao();
        if ("ALUNO".equals(sessao.perfil()) && !Objects.equals(sessao.userId(), id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode consultar seus próprios dados.");
        }
        if ("PROFISSIONAL".equals(sessao.perfil())
                && !atendimentoRepository.existsByAlunoIdAndProfissionalId(id, sessao.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não tem atendimento vinculado a este aluno.");
        }
        if (!List.of("ALUNO", "PROFISSIONAL", "ADMIN").contains(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem acesso aos dados do aluno.");
        }
        return aluno;
    }

    public Aluno atualizar(Long id, Aluno dados) {
        Aluno a = buscar(id);
        SessionContext.SessionInfo sessao = exigirSessao();
        boolean administrador = "ADMIN".equals(sessao.perfil());
        if (!administrador && !"ALUNO".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissão para alterar o aluno.");
        }
        String email = normalizarEmail(dados.getEmail());
        if (!Objects.equals(a.getEmail(), email)
                && usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        a.setEmail(email);
        validarTelefone(dados.getTelefone());
        a.setNome(dados.getNome());
        a.setTelefone(dados.getTelefone());
        a.setFotoUrl(dados.getFotoUrl());
        if (administrador) {
            a.setMatricula(dados.getMatricula());
            a.setCurso(dados.getCurso());
            a.setVinculo(dados.getVinculo());
        }
        // senha NÃO é alterada (regra 3 da Fase 2)
        return repository.save(a);
    }

    public void excluir(Long id) {
        Aluno a = buscar(id);
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())
                && !("ALUNO".equals(sessao.perfil()) && Objects.equals(sessao.userId(), id))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não pode excluir a conta de outro usuário.");
        }
        a.setNome("Conta excluída");
        a.setEmail("conta-excluida-" + id + "@invalid.local");
        a.setTelefone(null);
        a.setFotoUrl(null);
        a.setMatricula("EXCLUIDO-" + id);
        a.setCurso("ANONIMIZADO");
        a.setVinculo(null);
        a.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        a.setAtivo(false);
        repository.save(a);
    }

    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        usuarioService.alterarSenha(id, senhaAtual, novaSenha);
    }

    private void validarTelefone(String telefone) {
        try {
            TelefoneValidator.exigirValido(telefone);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail é obrigatório.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void exigirAdministrador() {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente administradores podem listar alunos.");
        }
    }

    private SessionContext.SessionInfo exigirSessao() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || sessao.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão expirada.");
        }
        return sessao;
    }
}