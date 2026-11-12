package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.AvisoInstitucional;
import com.br.unifor.paponline.service.AvisoInstitucionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avisos")
@RequiredArgsConstructor
public class AvisoInstitucionalController {

    private final AvisoInstitucionalService service;

    @PostMapping
    public AvisoInstitucional criar(@RequestBody AvisoInstitucional a) {
        return service.criar(a);
    }

    @GetMapping
    public List<AvisoInstitucional> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AvisoInstitucional buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public AvisoInstitucional atualizar(@PathVariable Long id, @RequestBody AvisoInstitucional dados) {
        return service.atualizar(id, dados);
    }

    @PatchMapping("/{id}/publicar")
    public AvisoInstitucional publicar(@PathVariable Long id) {
        return service.publicar(id);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}