package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.repository.ProfissionalRepository;
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
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository repository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    public Profissional criar(Profissional p) {
        exigirAdministrador();
        p.setEmail(normalizarEmail(p.getEmail()));
        if (usuarioRepository.existsByEmailIgnoreCase(p.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        if (repository.existsByCrp(p.getCrp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe um profissional com este CRP");
        }
        validarTelefone(p.getTelefone());
        try {
            SenhaValidator.exigirValida(p.getSenha());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        p.setSenha(passwordEncoder.encode(p.getSenha()));
        return repository.save(p);
    }

    public List<Profissional> listar() {
        exigirSessao();
        return repository.findAll();
    }

    public Profissional buscar(Long id) {
        exigirSessao();
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"));
    }

    public Profissional atualizar(Long id, Profissional dados) {
        Profissional p = buscar(id);
        SessionContext.SessionInfo sessao = exigirSessao();
        boolean administrador = "ADMIN".equals(sessao.perfil());
        if (!administrador && !("PROFISSIONAL".equals(sessao.perfil())
                && Objects.equals(sessao.userId(), id))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não pode alterar este profissional.");
        }
        String email = normalizarEmail(dados.getEmail());
        if (!Objects.equals(p.getEmail(), email)
                && usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        validarTelefone(dados.getTelefone());
        p.setNome(dados.getNome());
        p.setEmail(email);
        p.setTelefone(dados.getTelefone());
        p.setFotoUrl(dados.getFotoUrl());
        if (administrador) {
            p.setCrp(dados.getCrp());
            p.setCargo(dados.getCargo());
            if (dados.getAreaId() != null) {
                p.setAreaId(dados.getAreaId());
            }
        }
        return repository.save(p);
    }

    public void excluir(Long id) {
        SessionContext.SessionInfo sessao = exigirSessao();
        if (!"ADMIN".equals(sessao.perfil())
                && !("PROFISSIONAL".equals(sessao.perfil()) && Objects.equals(sessao.userId(), id))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você não pode excluir a conta de outro profissional.");
        }
        Profissional p = buscar(id);
        p.setNome("Profissional inativo");
        p.setEmail("conta-excluida-" + id + "@invalid.local");
        p.setTelefone(null);
        p.setFotoUrl(null);
        p.setCrp("ANONIMIZADO-" + id);
        p.setCargo("ANONIMIZADO");
        p.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        p.setAtivo(false);
        repository.save(p);
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
                    "Somente administradores podem gerenciar profissionais.");
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