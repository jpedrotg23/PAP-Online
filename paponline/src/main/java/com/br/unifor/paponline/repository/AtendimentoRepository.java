package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    List<Atendimento> findByAlunoId(Long alunoId);

    List<Atendimento> findByProfissionalId(Long profissionalId);

    boolean existsByAlunoIdAndAreaIdAndStatusIn(Long alunoId, Long areaId, Collection<String> status);

    boolean existsByAlunoIdAndProfissionalId(Long alunoId, Long profissionalId);

    List<Atendimento> findByStatusInAndDataHoraInicioBefore(Collection<String> status, LocalDateTime limite);

    List<Atendimento> findByStatusAndDataHoraInicioBetween(String status, LocalDateTime inicio, LocalDateTime fim);

    long countByAlunoIdAndStatusAndDataHoraInicioBetween(
            Long alunoId,
            String status,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    boolean existsByAreaId(Long areaId);

}