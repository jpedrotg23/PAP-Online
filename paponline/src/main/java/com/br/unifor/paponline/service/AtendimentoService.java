package com.br.unifor.paponline.service;

import com.br.unifor.paponline.entidades.Atendimento;
import com.br.unifor.paponline.repository.AtendimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AtendimentoService {

    private final AtendimentoRepository repository;

    public Atendimento criar(Atendimento a) {
        if (a.getDataHoraFim().isBefore(a.getDataHoraInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data/hora final deve ser depois da inicial");
        }
        return repository.save(a);
    }

    public List<Atendimento> listar() {
        return repository.findAll();
    }

    public Atendimento buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento não encontrado"));
    }

    public Atendimento atualizar(Long id, Atendimento dados) {
        Atendimento a = buscar(id);
        a.setAlunoId(dados.getAlunoId());
        a.setProfissionalId(dados.getProfissionalId());
        a.setDataHoraInicio(dados.getDataHoraInicio());
        a.setDataHoraFim(dados.getDataHoraFim());
        a.setModalidade(dados.getModalidade());
        a.setLocal(dados.getLocal());
        a.setStatus(dados.getStatus());
        return repository.save(a);
    }

    public Atendimento cancelar(Long id) {
        Atendimento a = buscar(id);
        a.setStatus("CANCELADO");
        return repository.save(a);
    }

    public Atendimento finalizar(Long id) {
        Atendimento a = buscar(id);
        a.setStatus("REALIZADO");
        return repository.save(a);
    }

    public void excluir(Long id) {
        Atendimento a = buscar(id);
        repository.delete(a);
    }

    public List<Atendimento> listarPorAluno(Long alunoId) {
        return repository.findByAlunoId(alunoId);
    }

    public List<Atendimento> listarPorProfissional(Long profissionalId) {
        return repository.findByProfissionalId(profissionalId);
    }
}