package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.AreaServico;
import com.br.unifor.paponline.repository.AreaServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
public class AreaServicoController {

    private final AreaServicoRepository repository;

    @GetMapping
    public List<AreaServico> listar() {
        return repository.findAll();
    }
}