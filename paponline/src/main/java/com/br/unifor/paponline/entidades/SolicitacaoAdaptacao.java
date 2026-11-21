package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_adaptacao")
@Getter
@Setter
@NoArgsConstructor
public class SolicitacaoAdaptacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String protocolo;

    @Column(nullable = false)
    private Long alunoId;

    private Long areaId;

    private Long profissionalId;

    @Column(nullable = false)
    private String tipo; // PROVA, PRAZO, MATERIAL, DISPENSA, LOCAL_PROVA, OUTRO

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String justificativa;

    @Column(nullable = false)
    private String status; // ENVIADA, EM_ANALISE, DEFERIDA, INDEFERIDA, PENDENTE_DOCUMENTACAO

    @Column(length = 2000)
    private String parecer;

    private LocalDate prazoParecer;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataParecer;
}