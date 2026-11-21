package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.repository.AdministradorRepository;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.util.SenhaValidator;
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
public class AdministradorService {

    private final AdministradorRepository repository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    public Administrador criar(Administrador a) {
        exigirAdministrador();
        a.setEmail(normalizarEmail(a.getEmail()));
        if (usuarioRepository.existsByEmailIgnoreCase(a.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        try {
            SenhaValidator.exigirValida(a.getSenha());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        a.setSenha(passwordEncoder.encode(a.getSenha()));
        return repository.save(a);
    }

    public List<Administrador> listar() {
        exigirAdministrador();
        return repository.findAll();
    }

    public Administrador buscar(Long id) {
        exigirAdministrador();
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Administrador não encontrado"));
    }

    public Administrador atualizar(Long id, Administrador dados) {
        exigirAdministrador();
        Administrador a = buscar(id);
        String email = normalizarEmail(dados.getEmail());
        if (!Objects.equals(a.getEmail(), email)
                && usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        a.setNome(dados.getNome());
        a.setEmail(email);
        a.setTelefone(dados.getTelefone());
        a.setCargo(dados.getCargo());
        return repository.save(a);
    }

    public void excluir(Long id) {
        exigirAdministrador();
        if (repository.countByAtivoTrue() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível desativar o último administrador ativo.");
        }
        Administrador a = buscar(id);
        a.setNome("Conta excluída");
        a.setEmail("conta-excluida-" + id + "@invalid.local");
        a.setTelefone(null);
        a.setFotoUrl(null);
        a.setCargo("ANONIMIZADO");
        a.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        a.setAtivo(false);
        repository.save(a);
    }

    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        usuarioService.alterarSenha(id, senhaAtual, novaSenha);
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail é obrigatório.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void exigirAdministrador() {
        SessionContext.SessionInfo sessao = SessionContext.get();
        if (sessao == null || !"ADMIN".equals(sessao.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente administradores podem gerenciar contas administrativas.");
        }
    }
}