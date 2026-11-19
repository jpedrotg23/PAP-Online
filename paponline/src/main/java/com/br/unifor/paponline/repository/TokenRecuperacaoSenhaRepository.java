package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.TokenRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, Long> {

    Optional<TokenRecuperacaoSenha> findFirstByIdentificadorAndCodigoAndUsadoFalseOrderByCriadoEmDesc(
            String identificador, String codigo);

    Optional<TokenRecuperacaoSenha> findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc(
            String identificador);
}