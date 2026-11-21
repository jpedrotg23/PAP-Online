package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    long countByAtivoTrue();
}