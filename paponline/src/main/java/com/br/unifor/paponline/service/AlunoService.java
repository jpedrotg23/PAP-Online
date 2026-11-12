package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.repository.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository repository;

    public Aluno criar(Aluno aluno) {
        if (repository.existsByMatricula(aluno.getMatricula())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um aluno com esta matrícula");
        }
        if (repository.existsByEmail(aluno.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        return repository.save(aluno);
    }

    public List<Aluno> listar() {
        return repository.findAll();
    }

    public Aluno buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }

    public Aluno atualizar(Long id, Aluno dados) {
        Aluno a = buscar(id);
        a.setNome(dados.getNome());
        a.setEmail(dados.getEmail());
        a.setTelefone(dados.getTelefone());
        if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
            a.setSenha(dados.getSenha());
        }
        a.setMatricula(dados.getMatricula());
        a.setCurso(dados.getCurso());
        a.setVinculo(dados.getVinculo());
        return repository.save(a);
    }

    public void excluir(Long id) {
        Aluno a = buscar(id);
        repository.delete(a);
    }
}