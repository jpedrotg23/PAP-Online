package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByMatricula(String matricula);

    boolean existsByEmail(String email);

    Optional<Aluno> findByMatricula(String matricula);

    List<Aluno> findByAtivoTrue();
}