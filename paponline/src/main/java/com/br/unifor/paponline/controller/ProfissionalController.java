package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.service.ProfissionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService service;

    @PostMapping
    public Profissional criar(@RequestBody Profissional p) {
        return service.criar(p);
    }

    @GetMapping
    public List<Profissional> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Profissional buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public Profissional atualizar(@PathVariable Long id, @RequestBody Profissional dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}