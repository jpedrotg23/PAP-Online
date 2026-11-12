package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.AvisoInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisoInstitucionalRepository extends JpaRepository<AvisoInstitucional, Long> {
    List<AvisoInstitucional> findByStatus(String status);
}