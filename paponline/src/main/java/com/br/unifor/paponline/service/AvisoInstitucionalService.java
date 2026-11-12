package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.AvisoInstitucional;
import com.br.unifor.paponline.repository.AvisoInstitucionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvisoInstitucionalService {

    private final AvisoInstitucionalRepository repository;

    public AvisoInstitucional criar(AvisoInstitucional a) {
        a.setCriadoEm(LocalDateTime.now());
        if (a.getStatus() == null) {
            a.setStatus("RASCUNHO");
        }
        return repository.save(a);
    }

    public List<AvisoInstitucional> listar() {
        return repository.findAll();
    }

    public AvisoInstitucional buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aviso não encontrado"));
    }

    public AvisoInstitucional atualizar(Long id, AvisoInstitucional dados) {
        AvisoInstitucional a = buscar(id);
        a.setTitulo(dados.getTitulo());
        a.setConteudo(dados.getConteudo());
        a.setCategoria(dados.getCategoria());
        a.setDestaque(dados.getDestaque());
        a.setPublicoAlvo(dados.getPublicoAlvo());
        a.setStatus(dados.getStatus());
        return repository.save(a);
    }

    public AvisoInstitucional publicar(Long id) {
        AvisoInstitucional a = buscar(id);
        a.setStatus("PUBLICADO");
        return repository.save(a);
    }

    public void excluir(Long id) {
        AvisoInstitucional a = buscar(id);
        repository.delete(a);
    }
}