package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.TokenRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, Long> {

    Optional<TokenRecuperacaoSenha> findFirstByIdentificadorAndCodigoAndUsadoFalseOrderByCriadoEmDesc(
            String identificador, String codigo);

    Optional<TokenRecuperacaoSenha> findFirstByIdentificadorAndUsadoFalseOrderByCriadoEmDesc(
            String identificador);

    Optional<TokenRecuperacaoSenha> findFirstByIdentificadorOrderByCriadoEmDesc(String identificador);

    List<TokenRecuperacaoSenha> findByIdentificadorAndUsadoFalse(String identificador);
}