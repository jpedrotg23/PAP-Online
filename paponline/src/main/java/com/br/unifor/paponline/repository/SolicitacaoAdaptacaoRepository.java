package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.SolicitacaoAdaptacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SolicitacaoAdaptacaoRepository extends JpaRepository<SolicitacaoAdaptacao, Long> {
    List<SolicitacaoAdaptacao> findByAlunoId(Long alunoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitacaoAdaptacao s where s.id = :id")
    Optional<SolicitacaoAdaptacao> findByIdForUpdate(@Param("id") Long id);
    List<SolicitacaoAdaptacao> findByProfissionalId(Long profissionalId);
    List<SolicitacaoAdaptacao> findByAreaId(Long areaId);
    List<SolicitacaoAdaptacao> findByStatus(String status);
    boolean existsByProtocolo(String protocolo);
    List<SolicitacaoAdaptacao> findByProtocoloStartingWith(String prefixo);
}