package com.br.unifor.paponline.service;

import com.br.unifor.paponline.config.SessionContext;
import com.br.unifor.paponline.entidades.Usuario;
import com.br.unifor.paponline.repository.UsuarioRepository;
import com.br.unifor.paponline.util.SenhaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Usuario criar(Usuario usuario) {
        if (repository.existsByEmail(usuario.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        SenhaValidator.exigirValida(usuario.getSenha());
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return repository.save(usuario);
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    public Usuario atualizar(Long id, Usuario dados) {
        Usuario u = buscar(id);
        u.setNome(dados.getNome());
        u.setEmail(dados.getEmail());
        u.setTelefone(dados.getTelefone());
        // senha NÃO é alterada por aqui (regra 3 da Fase 2)
        return repository.save(u);
    }

    public void excluir(Long id) {
        Usuario u = buscar(id);
        repository.delete(u);
    }

    public Optional<Usuario> autenticar(String identificador, String senha) {
        Optional<Usuario> opt = repository.findByEmailOrTelefone(identificador, identificador);
        if (opt.isEmpty()) return Optional.empty();

        Usuario u = opt.get();
        if (!passwordEncoder.matches(senha, u.getSenha())) return Optional.empty();

        if (Boolean.FALSE.equals(u.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Conta desativada. Entre em contato com a coordenação do PAP.");
        }
        return Optional.of(u);
    }

    /**
     * Troca de senha do PRÓPRIO usuário. Regra da Fase 2 (item 4): só o próprio troca.
     */
    public void alterarSenha(Long idAlvo, String senhaAtual, String novaSenha) {
        SessionContext.SessionInfo ctx = SessionContext.get();
        if (ctx == null || !ctx.userId().equals(idAlvo)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você só pode alterar a sua própria senha.");
        }

        Usuario u = buscar(idAlvo);

        if (!passwordEncoder.matches(senhaAtual, u.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        }

        SenhaValidator.exigirValida(novaSenha);

        if (passwordEncoder.matches(novaSenha, u.getSenha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A nova senha deve ser diferente da atual.");
        }

        u.setSenha(passwordEncoder.encode(novaSenha));
        repository.save(u);
    }
}