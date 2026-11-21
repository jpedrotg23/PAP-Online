package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.AlterarSenhaRequest;
import com.br.unifor.paponline.dto.AlunoResponse;
import com.br.unifor.paponline.entidades.Aluno;
import com.br.unifor.paponline.service.AlunoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService service;

    @PostMapping
    public AlunoResponse criar(@RequestBody Aluno aluno) {
        return AlunoResponse.from(service.criar(aluno));
    }

    @GetMapping
    public List<AlunoResponse> listar() {
        return service.listar().stream().map(AlunoResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id) {
        return AlunoResponse.from(service.buscar(id));
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable Long id, @RequestBody Aluno dados) {
        return AlunoResponse.from(service.atualizar(id, dados));
    }

    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@PathVariable Long id, @Valid @RequestBody AlterarSenhaRequest req) {
        service.alterarSenha(id, req.senhaAtual(), req.novaSenha());
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id, HttpSession session) {
        service.excluir(id);
        if (session.getAttribute("usuarioId") instanceof Number userId && userId.longValue() == id) {
            session.invalidate();
        }
    }
}