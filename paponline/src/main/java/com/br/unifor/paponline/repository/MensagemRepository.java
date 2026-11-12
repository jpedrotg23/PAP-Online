package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByConversaIdOrderByEnviadaEmAsc(Long conversaId);
}