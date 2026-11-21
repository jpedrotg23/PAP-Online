package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.NotificacaoRequest;
import com.br.unifor.paponline.entidades.Notificacao;
import com.br.unifor.paponline.service.NotificacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notificacao criar(@Valid @RequestBody NotificacaoRequest req) {
        return service.criarManual(req.usuarioId(), req.titulo(), req.mensagem());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacao> listarPorUsuario(@PathVariable Long usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/count")
    public long contarNaoLidas(@PathVariable Long usuarioId) {
        return service.contarNaoLidas(usuarioId);
    }

    @PatchMapping("/{id}/lida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoLida(@PathVariable Long id) {
        service.marcarComoLida(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}