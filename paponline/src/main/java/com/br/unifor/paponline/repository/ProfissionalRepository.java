package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    boolean existsByCrp(String crp);
}