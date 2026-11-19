package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.repository.AlunoRepository;
import com.br.unifor.paponline.util.SenhaValidator;
import com.br.unifor.paponline.util.TelefoneValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository repository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public Aluno criar(Aluno aluno) {
        if (repository.existsByMatricula(aluno.getMatricula())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um aluno com esta matrícula");
        }
        if (repository.existsByEmail(aluno.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário com este e-mail");
        }
        validarTelefone(aluno.getTelefone());
        SenhaValidator.exigirValida(aluno.getSenha());
        aluno.setSenha(passwordEncoder.encode(aluno.getSenha()));
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
        validarTelefone(dados.getTelefone());
        a.setNome(dados.getNome());
        a.setEmail(dados.getEmail());
        a.setTelefone(dados.getTelefone());
        a.setMatricula(dados.getMatricula());
        a.setCurso(dados.getCurso());
        a.setVinculo(dados.getVinculo());
        // senha NÃO é alterada (regra 3 da Fase 2)
        return repository.save(a);
    }

    public void excluir(Long id) {
        Aluno a = buscar(id);
        repository.delete(a);
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
}