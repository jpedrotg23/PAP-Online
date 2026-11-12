package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.repository.AdministradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministradorService {

    private final AdministradorRepository repository;

    public Administrador criar(Administrador a) {
        return repository.save(a);
    }

    public List<Administrador> listar() {
        return repository.findAll();
    }

    public Administrador buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado"));
    }

    public Administrador atualizar(Long id, Administrador dados) {
        Administrador a = buscar(id);
        a.setNome(dados.getNome());
        a.setEmail(dados.getEmail());
        a.setTelefone(dados.getTelefone());
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            a.setSenha(dados.getSenha());
        }
        a.setCargo(dados.getCargo());
        return repository.save(a);
    }

    public void excluir(Long id) {
        Administrador a = buscar(id);
        repository.delete(a);
    }
}