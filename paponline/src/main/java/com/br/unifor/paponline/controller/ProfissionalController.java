package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.dto.AlterarSenhaRequest;
import com.br.unifor.paponline.dto.ProfissionalCadastroRequest;
import com.br.unifor.paponline.dto.ProfissionalResponse;
import com.br.unifor.paponline.entidades.Profissional;
import com.br.unifor.paponline.service.ProfissionalService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService service;

    @PostMapping
    public ProfissionalResponse criar(@Valid @RequestBody ProfissionalCadastroRequest request) {
        return ProfissionalResponse.from(service.criar(request.toEntity()));
    }

    @GetMapping
    public List<ProfissionalResponse> listar() {
        return service.listar().stream().map(ProfissionalResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscar(@PathVariable Long id) {
        return ProfissionalResponse.from(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable Long id, @RequestBody Profissional dados) {
        return ProfissionalResponse.from(service.atualizar(id, dados));
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