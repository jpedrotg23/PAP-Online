package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.AreaServicoRequest;
import com.br.unifor.paponline.entidades.AreaServico;
import com.br.unifor.paponline.service.AreaServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
public class AreaServicoController {

    private final AreaServicoService service;

    @GetMapping
    public List<AreaServico> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AreaServico buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AreaServico criar(@Valid @RequestBody AreaServicoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public AreaServico atualizar(@PathVariable Long id, @Valid @RequestBody AreaServicoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}