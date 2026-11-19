package com.br.unifor.paponline.service;

import com.br.unifor.paponline.dto.BloqueioRequest;
import com.br.unifor.paponline.dto.HorarioDisponivelRequest;
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

    public HorarioDisponivel criar(HorarioDisponivelRequest req) {
        if (req.horaFim().isBefore(req.horaInicio()) || req.horaFim().equals(req.horaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hora final deve ser depois da hora de início");
        }

        HorarioDisponivel h = new HorarioDisponivel();
        h.setProfissionalId(req.profissionalId());
        h.setData(req.data());
        h.setHoraInicio(req.horaInicio());
        h.setHoraFim(req.horaFim());
        h.setDisponivel(true);
        h.setBloqueado(false);

        return repository.save(h);
    }

    public HorarioDisponivel editar(Long id, HorarioDisponivelRequest req) {
        HorarioDisponivel h = buscar(id);
        if (Boolean.FALSE.equals(h.getDisponivel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horário já reservado. Cancele o atendimento antes de editar.");
        }
        if (req.horaFim().isBefore(req.horaInicio()) || req.horaFim().equals(req.horaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hora final deve ser depois da hora de início");
        }
        h.setProfissionalId(req.profissionalId());
        h.setData(req.data());
        h.setHoraInicio(req.horaInicio());
        h.setHoraFim(req.horaFim());
        return repository.save(h);
    }

    public HorarioDisponivel bloquear(Long id, BloqueioRequest req) {
        HorarioDisponivel h = buscar(id);
        if (Boolean.FALSE.equals(h.getDisponivel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não é possível bloquear um horário já reservado");
        }
        h.setBloqueado(true);
        h.setMotivoBloqueio(req.motivo());
        return repository.save(h);
    }

    public HorarioDisponivel liberar(Long id) {
        HorarioDisponivel h = buscar(id);
        h.setBloqueado(false);
        h.setMotivoBloqueio(null);
        return repository.save(h);
    }

    public List<HorarioDisponivel> listar() {
        return repository.findAll();
    }

    public List<HorarioDisponivel> listarPorProfissional(Long profissionalId) {
        return repository.findByProfissionalId(profissionalId);
    }

    public List<HorarioDisponivel> listarDisponiveis(Long profissionalId) {
        return repository.findByProfissionalIdAndDisponivelTrueAndBloqueadoFalse(profissionalId);
    }

    public HorarioDisponivel buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Horário não encontrado"));
    }

    public void excluir(Long id) {
        HorarioDisponivel h = buscar(id);
        if (Boolean.FALSE.equals(h.getDisponivel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Horário já reservado. Cancele o atendimento antes.");
        }
        repository.delete(h);
    }
}