package com.br.unifor.paponline.repository;

import com.br.unifor.paponline.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByTelefone(String telefone);

    Optional<Usuario> findByEmailOrTelefone(String email, String telefone);

    boolean existsByEmail(String email);

    List<Usuario> findByAtivoTrue();
}