package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Getter
@Setter
@NoArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String titulo;

    private String mensagem;

    private String tipo;

    private Long referenciaId;

    @Column(nullable = false)
    private Boolean lida = false;

    private LocalDateTime criadaEm = LocalDateTime.now();

    private LocalDateTime lidaEm;
}