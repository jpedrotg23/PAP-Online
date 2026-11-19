package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Optional<Conversa> findByAlunoIdAndProfissionalId(Long alunoId, Long profissionalId);

    Optional<Conversa> findByAlunoIdAndAdminId(Long alunoId, Long adminId);

    Optional<Conversa> findByProfissionalIdAndAdminId(Long profissionalId, Long adminId);

    List<Conversa> findByAlunoId(Long alunoId);

    List<Conversa> findByProfissionalId(Long profissionalId);

    List<Conversa> findByAdminId(Long adminId);
}