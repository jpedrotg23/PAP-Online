package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_recuperacao")
@Getter
@Setter
@NoArgsConstructor
public class TokenRecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String identificador;

    @Column(nullable = false, length = 100)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private Boolean usado = false;

    @Column(nullable = false)
    private Integer tentativas = 0;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}