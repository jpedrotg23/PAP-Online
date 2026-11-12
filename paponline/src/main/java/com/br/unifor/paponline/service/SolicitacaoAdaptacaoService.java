package com.br.unifor.paponline.service;

import com.br.unifor.paponline.repository.SolicitacaoAdaptacaoRepository;
import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoAdaptacaoService {

    private final SolicitacaoAdaptacaoRepository repository;

    public SolicitacaoAdaptacao criar(SolicitacaoAdaptacao s) {
        s.setDataEnvio(LocalDateTime.now());
        if (s.getStatus() == null) {
            s.setStatus("ENVIADA");
        }
        return repository.save(s);
    }

    public List<SolicitacaoAdaptacao> listar() {
        return repository.findAll();
    }

    public SolicitacaoAdaptacao buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada"));
    }

    public SolicitacaoAdaptacao atualizar(Long id, SolicitacaoAdaptacao dados) {
        SolicitacaoAdaptacao s = buscar(id);
        s.setTipo(dados.getTipo());
        s.setTitulo(dados.getTitulo());
        s.setJustificativa(dados.getJustificativa());
        s.setStatus(dados.getStatus());
        return repository.save(s);
    }

    public SolicitacaoAdaptacao analisar(Long id, String parecer, String novoStatus, Long profissionalId) {
        SolicitacaoAdaptacao s = buscar(id);
        s.setParecer(parecer);
        s.setStatus(novoStatus);
        s.setProfissionalId(profissionalId);
        s.setDataParecer(LocalDateTime.now());
        return repository.save(s);
    }

    public void excluir(Long id) {
        SolicitacaoAdaptacao s = buscar(id);
        repository.delete(s);
    }

    public List<SolicitacaoAdaptacao> listarPorAluno(Long alunoId) {
        return repository.findByAlunoId(alunoId);
    }

    public List<SolicitacaoAdaptacao> listarPorStatus(String status) {
        return repository.findByStatus(status);
    }
}