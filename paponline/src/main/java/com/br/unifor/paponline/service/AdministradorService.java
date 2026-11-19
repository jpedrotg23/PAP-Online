package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.repository.AdministradorRepository;
import com.br.unifor.paponline.util.SenhaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final AdministradorRepository repository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public Administrador criar(Administrador a) {
        SenhaValidator.exigirValida(a.getSenha());
        a.setSenha(passwordEncoder.encode(a.getSenha()));
        return repository.save(a);
    }

    public List<Administrador> listar() {
        return repository.findAll();
    }

    public Administrador buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Administrador não encontrado"));
    }

    public Administrador atualizar(Long id, Administrador dados) {
        Administrador a = buscar(id);
        a.setNome(dados.getNome());
        a.setEmail(dados.getEmail());
        a.setTelefone(dados.getTelefone());
        a.setCargo(dados.getCargo());
        return repository.save(a);
    }

    public void excluir(Long id) {
        Administrador a = buscar(id);
        repository.delete(a);
    }

    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        usuarioService.alterarSenha(id, senhaAtual, novaSenha);
    }
}