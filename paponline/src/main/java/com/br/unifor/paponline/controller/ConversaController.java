package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.Conversa;
import com.br.unifor.paponline.entidades.Mensagem;
import com.br.unifor.paponline.service.ConversaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversas")
@RequiredArgsConstructor
public class ConversaController {

    private final ConversaService service;

    @PostMapping
    public Conversa obterOuCriar(@RequestParam Long alunoId, @RequestParam Long profissionalId) {
        return service.obterOuCriar(alunoId, profissionalId);
    }

    @GetMapping("/aluno/{alunoId}")
    public List<Conversa> listarPorAluno(@PathVariable Long alunoId) {
        return service.listarPorAluno(alunoId);
    }

    @GetMapping("/profissional/{profissionalId}")
    public List<Conversa> listarPorProfissional(@PathVariable Long profissionalId) {
        return service.listarPorProfissional(profissionalId);
    }

    @GetMapping("/{conversaId}/mensagens")
    public List<Mensagem> listarMensagens(@PathVariable Long conversaId) {
        return service.listarMensagens(conversaId);
    }

    @PostMapping("/{conversaId}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public Mensagem enviar(@PathVariable Long conversaId, @RequestBody Map<String, Object> body) {
        Long remetenteId = ((Number) body.get("remetenteId")).longValue();
        String conteudo = (String) body.get("conteudo");
        return service.enviar(conversaId, remetenteId, conteudo);
    }

    @PatchMapping("/mensagens/{mensagemId}/lida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoLida(@PathVariable Long mensagemId) {
        service.marcarComoLida(mensagemId);
    }
}