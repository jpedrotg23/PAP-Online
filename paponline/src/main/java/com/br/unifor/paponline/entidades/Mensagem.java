package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens")
@Getter
@Setter
@NoArgsConstructor
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversaId;

    @Column(nullable = false)
    private Long remetenteId;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    private Boolean lida = false;

    private LocalDateTime enviadaEm = LocalDateTime.now();
}