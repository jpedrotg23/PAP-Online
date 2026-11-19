package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    boolean existsByCrp(String crp);

    Optional<Profissional> findByCrp(String crp);

    List<Profissional> findByAtivoTrue();
}