package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.HorarioDisponivel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Long> {

    List<HorarioDisponivel> findByProfissionalId(Long profissionalId);

    List<HorarioDisponivel> findByProfissionalIdAndDisponivelTrueAndBloqueadoFalse(Long profissionalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from HorarioDisponivel h where h.id = :id")
    Optional<HorarioDisponivel> findByIdForUpdate(@Param("id") Long id);
}
