package com.br.unifor.paponline.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TermoController {

    @Value("${pap.consent.version:1.0}")
    private String versao;

    @GetMapping("/termo")
    public TermoResponse termo() {
        return new TermoResponse(versao);
    }

    public record TermoResponse(String versao) {}
}
