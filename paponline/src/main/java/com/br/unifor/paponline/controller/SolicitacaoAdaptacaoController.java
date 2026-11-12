package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.service.SolicitacaoAdaptacaoService;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoAdaptacaoController {

    private final SolicitacaoAdaptacaoService service;

    @PostMapping
    public SolicitacaoAdaptacao criar(@RequestBody SolicitacaoAdaptacao s) {
        return service.criar(s);
    }

    @GetMapping
    public List<SolicitacaoAdaptacao> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SolicitacaoAdaptacao buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public SolicitacaoAdaptacao atualizar(@PathVariable Long id, @RequestBody SolicitacaoAdaptacao dados) {
        return service.atualizar(id, dados);
    }

    @PatchMapping("/{id}/analisar")
    public SolicitacaoAdaptacao analisar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String parecer = (String) body.get("parecer");
        String status = (String) body.get("status");
        Long profissionalId = body.get("profissionalId") != null
                ? ((Number) body.get("profissionalId")).longValue()
                : null;
        return service.analisar(id, parecer, status, profissionalId);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/aluno/{alunoId}")
    public List<SolicitacaoAdaptacao> listarPorAluno(@PathVariable Long alunoId) {
        return service.listarPorAluno(alunoId);
    }
}