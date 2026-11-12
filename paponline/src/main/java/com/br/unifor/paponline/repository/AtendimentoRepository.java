package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {
    List<Atendimento> findByAlunoId(Long alunoId);
    List<Atendimento> findByProfissionalId(Long profissionalId);
}