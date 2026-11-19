package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String telefone;
    private String senha;
    private String fotoUrl;

    private Boolean ativo = true;

    private LocalDateTime dataCadastro = LocalDateTime.now();
}