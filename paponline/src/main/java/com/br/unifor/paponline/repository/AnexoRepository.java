package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    List<Anexo> findBySolicitacaoId(Long solicitacaoId);
}