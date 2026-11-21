package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.AlterarSenhaRequest;
import com.br.unifor.paponline.dto.AdministradorResponse;
import com.br.unifor.paponline.entidades.Administrador;
import com.br.unifor.paponline.service.AdministradorService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdministradorController {

    private final AdministradorService service;

    @PostMapping
    public AdministradorResponse criar(@RequestBody Administrador a) {
        return AdministradorResponse.from(service.criar(a));
    }

    @GetMapping
    public List<AdministradorResponse> listar() {
        return service.listar().stream().map(AdministradorResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public AdministradorResponse buscar(@PathVariable Long id) {
        return AdministradorResponse.from(service.buscar(id));
    }

    @PutMapping("/{id}")
    public AdministradorResponse atualizar(@PathVariable Long id, @RequestBody Administrador dados) {
        return AdministradorResponse.from(service.atualizar(id, dados));
    }

    @PatchMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@PathVariable Long id, @Valid @RequestBody AlterarSenhaRequest req) {
        service.alterarSenha(id, req.senhaAtual(), req.novaSenha());
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id, HttpSession session) {
        service.excluir(id);
        if (session.getAttribute("usuarioId") instanceof Number userId && userId.longValue() == id) {
            session.invalidate();
        }
    }
}