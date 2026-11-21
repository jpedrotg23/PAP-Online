package com.br.unifor.paponline.controller;

import com.br.unifor.paponline.entidades.Anexo;
import com.br.unifor.paponline.dto.AnexoResponse;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import com.br.unifor.paponline.service.AnexoService;
import com.br.unifor.paponline.service.AnexoService.AnexoDownload;
import com.br.unifor.paponline.service.SolicitacaoAdaptacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoAdaptacaoController {

    private final SolicitacaoAdaptacaoService service;
    private final AnexoService anexoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoAdaptacao criar(@RequestBody SolicitacaoAdaptacao s) {
        return service.criar(s);
    }

    @GetMapping
    public List<SolicitacaoAdaptacao> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SolicitacaoAdaptacao buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public SolicitacaoAdaptacao atualizar(@PathVariable Long id, @RequestBody SolicitacaoAdaptacao dados) {
        return service.atualizar(id, dados);
    }

    @PatchMapping("/{id}/analisar")
    public SolicitacaoAdaptacao analisar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.analisar(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/aluno/{alunoId}")
    public List<SolicitacaoAdaptacao> listarPorAluno(@PathVariable Long alunoId) {
        return service.listarPorAluno(alunoId);
    }

    @PostMapping(value = "/{id}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AnexoResponse uploadAnexo(
            @PathVariable Long id,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        MultipartFile escolhido = (arquivo != null && !arquivo.isEmpty()) ? arquivo : file;
        return AnexoResponse.from(anexoService.salvar(id, escolhido));
    }

    @GetMapping("/{id}/anexos")
    public List<AnexoResponse> listarAnexos(@PathVariable Long id) {
        return anexoService.listarPorSolicitacao(id).stream().map(AnexoResponse::from).toList();
    }

    @GetMapping("/{id}/anexos/{anexoId}")
    public ResponseEntity<Resource> baixarAnexo(
            @PathVariable Long id,
            @PathVariable Long anexoId) {
        AnexoDownload download = anexoService.obterParaDownload(id, anexoId);
        Anexo anexo = download.anexo();

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(anexo.getTipoMime());
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(anexo.getNomeArquivo(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentLength(anexo.getTamanho())
                .body(download.resource());
    }
}