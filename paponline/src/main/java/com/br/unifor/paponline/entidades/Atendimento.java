package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long alunoId;
    private Long profissionalId;
    private Long areaId;       // NOVO — obrigatório na regra de negócio (validado no DTO)
    private Long horarioId;    // NOVO — opcional

    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;

    private String modalidade;
    private String local;

    private String status;
    private LocalDateTime dataCriacao;
}