package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.AtendimentoRequest;
import com.br.unifor.paponline.dto.RemarcarAtendimentoRequest;
import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.service.AtendimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Atendimento criar(@Valid @RequestBody AtendimentoRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<Atendimento> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Atendimento buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/aluno/{alunoId}")
    public List<Atendimento> listarPorAluno(@PathVariable Long alunoId) {
        return service.listarPorAluno(alunoId);
    }

    @GetMapping("/profissional/{profissionalId}")
    public List<Atendimento> listarPorProfissional(@PathVariable Long profissionalId) {
        return service.listarPorProfissional(profissionalId);
    }

    @PutMapping("/{id}")
    public Atendimento atualizar(@PathVariable Long id, @Valid @RequestBody AtendimentoRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/cancelar")
    public Atendimento cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PatchMapping("/{id}/remarcar")
    public Atendimento remarcar(
            @PathVariable Long id,
            @Valid @RequestBody RemarcarAtendimentoRequest req
    ) {
        return service.remarcar(id, req);
    }

    @PatchMapping("/{id}/finalizar")
    public Atendimento finalizar(@PathVariable Long id) {
        return service.finalizar(id);
    }

    @PatchMapping("/{id}/falta")
    public Atendimento registrarFalta(@PathVariable Long id) {
        return service.registrarFalta(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
