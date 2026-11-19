package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.repository.ProfissionalRepository;
import com.br.unifor.paponline.util.SenhaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository repository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public Profissional criar(Profissional p) {
        if (repository.existsByCrp(p.getCrp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Já existe um profissional com este CRP");
        }
        SenhaValidator.exigirValida(p.getSenha());
        p.setSenha(passwordEncoder.encode(p.getSenha()));
        return repository.save(p);
    }

    public List<Profissional> listar() {
        return repository.findAll();
    }

    public Profissional buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"));
    }

    public Profissional atualizar(Long id, Profissional dados) {
        Profissional p = buscar(id);
        p.setNome(dados.getNome());
        p.setEmail(dados.getEmail());
        p.setTelefone(dados.getTelefone());
        p.setCrp(dados.getCrp());
        p.setCargo(dados.getCargo());
        if (dados.getAreaId() != null) {
            p.setAreaId(dados.getAreaId());
        }

        return repository.save(p);
    }

    public void excluir(Long id) {
        Profissional p = buscar(id);
        repository.delete(p);
    }

    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        usuarioService.alterarSenha(id, senhaAtual, novaSenha);
    }
}