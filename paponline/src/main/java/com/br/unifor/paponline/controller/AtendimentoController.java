package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import com.br.unifor.paponline.service.AtendimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService service;
    private final AtendimentoRepository atendimentoRepository;

    @PostMapping
    public Atendimento criar(@RequestBody Atendimento a) {
        return service.criar(a);
    }

    @GetMapping
    public List<Atendimento> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Atendimento buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public Atendimento atualizar(@PathVariable Long id, @RequestBody Atendimento dados) {
        return service.atualizar(id, dados);
    }

    @PatchMapping("/{id}/cancelar")
    public Atendimento cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PatchMapping("/{id}/finalizar")
    public Atendimento finalizar(@PathVariable Long id) {
        return service.finalizar(id);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/aluno/{alunoId}")
    public List<Atendimento> listarPorAluno(@PathVariable Long alunoId) {
        return service.listarPorAluno(alunoId);
    }

    @GetMapping("/profissional/{profissionalId}")
    public List<Atendimento> listarPorProfissional(@PathVariable Long profissionalId) {
        return service.listarPorProfissional(profissionalId);
    }
}