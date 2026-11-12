package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.HorarioDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, Long> {
    List<HorarioDisponivel> findByProfissionalId(Long profissionalId);
    List<HorarioDisponivel> findByProfissionalIdAndData(Long profissionalId, LocalDate data);
    List<HorarioDisponivel> findByProfissionalIdAndDisponivelTrue(Long profissionalId);
}