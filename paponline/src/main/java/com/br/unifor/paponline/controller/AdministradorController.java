package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.AlterarSenhaRequest;
import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.service.AdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService service;

    @PostMapping
    public Administrador criar(@RequestBody Administrador a) {
        return service.criar(a);
    }

    @GetMapping
    public List<Administrador> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Administrador buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public Administrador atualizar(@PathVariable Long id, @RequestBody Administrador dados) {
        return service.atualizar(id, dados);
    }

    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@PathVariable Long id, @Valid @RequestBody AlterarSenhaRequest req) {
        service.alterarSenha(id, req.senhaAtual(), req.novaSenha());
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}