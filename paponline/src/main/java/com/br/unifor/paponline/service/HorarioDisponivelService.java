package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.HorarioDisponivel;
import com.br.unifor.paponline.repository.HorarioDisponivelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository repository;

    public HorarioDisponivel criar(HorarioDisponivel h) {
        return repository.save(h);
    }

    public List<HorarioDisponivel> listar() {
        return repository.findAll();
    }

    public List<HorarioDisponivel> listarPorProfissional(Long profissionalId) {
        return repository.findByProfissionalId(profissionalId);
    }

    public List<HorarioDisponivel> listarDisponiveis(Long profissionalId) {
        return repository.findByProfissionalIdAndDisponivelTrue(profissionalId);
    }

    public HorarioDisponivel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horário não encontrado"));
    }

    public void excluir(Long id) {
        HorarioDisponivel h = buscar(id);
        repository.delete(h);
    }
}