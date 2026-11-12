package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.HorarioDisponivel;
import com.br.unifor.paponline.service.HorarioDisponivelService;
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
    public HorarioDisponivel criar(@RequestBody HorarioDisponivel h) {
        return service.criar(h);
    }

    @GetMapping
    public List<HorarioDisponivel> listar() {
        return service.listar();
    }

    @GetMapping("/profissional/{profissionalId}")
    public List<HorarioDisponivel> listarPorProfissional(@PathVariable Long profissionalId) {
        return service.listarPorProfissional(profissionalId);
    }

    @GetMapping("/profissional/{profissionalId}/disponiveis")
    public List<HorarioDisponivel> listarDisponiveis(@PathVariable Long profissionalId) {
        return service.listarDisponiveis(profissionalId);
    }

    @GetMapping("/{id}")
    public HorarioDisponivel buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}