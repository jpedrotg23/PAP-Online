package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.BloqueioRequest;
import com.br.unifor.paponline.dto.HorarioDisponivelRequest;
import com.br.unifor.paponline.entidades.HorarioDisponivel;
import com.br.unifor.paponline.service.HorarioDisponivelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/horarios")
@RequiredArgsConstructor
public class HorarioDisponivelController {

    private final HorarioDisponivelService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HorarioDisponivel criar(@Valid @RequestBody HorarioDisponivelRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<HorarioDisponivel> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public HorarioDisponivel buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/profissional/{profissionalId}")
    public List<HorarioDisponivel> listarPorProfissional(@PathVariable Long profissionalId) {
        return service.listarPorProfissional(profissionalId);
    }

    @GetMapping("/profissional/{profissionalId}/disponiveis")
    public List<HorarioDisponivel> listarDisponiveis(@PathVariable Long profissionalId) {
        return service.listarDisponiveis(profissionalId);
    }

    @PutMapping("/{id}")
    public HorarioDisponivel editar(@PathVariable Long id, @Valid @RequestBody HorarioDisponivelRequest req) {
        return service.editar(id, req);
    }

    @PatchMapping("/{id}/bloquear")
    public HorarioDisponivel bloquear(@PathVariable Long id, @Valid @RequestBody BloqueioRequest req) {
        return service.bloquear(id, req);
    }

    @PatchMapping("/{id}/liberar")
    public HorarioDisponivel liberar(@PathVariable Long id) {
        return service.liberar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}