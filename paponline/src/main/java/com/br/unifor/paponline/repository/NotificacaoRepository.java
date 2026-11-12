package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);
    List<Notificacao> findByUsuarioIdAndLidaFalse(Long usuarioId);
    long countByUsuarioIdAndLidaFalse(Long usuarioId);
}