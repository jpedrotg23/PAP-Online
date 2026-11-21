package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoAdaptacaoRepository extends JpaRepository<SolicitacaoAdaptacao, Long> {
    List<SolicitacaoAdaptacao> findByAlunoId(Long alunoId);
    List<SolicitacaoAdaptacao> findByProfissionalId(Long profissionalId);
    List<SolicitacaoAdaptacao> findByAreaId(Long areaId);
    List<SolicitacaoAdaptacao> findByStatus(String status);
    boolean existsByProtocolo(String protocolo);
    List<SolicitacaoAdaptacao> findByProtocoloStartingWith(String prefixo);
}