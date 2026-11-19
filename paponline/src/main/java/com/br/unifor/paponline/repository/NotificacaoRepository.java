package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    long countByUsuarioIdAndLidaFalse(Long usuarioId);

    boolean existsByUsuarioIdAndTipoAndReferenciaId(Long usuarioId, String tipo, Long referenciaId);
}