package com.br.unifor.paponline.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AtendimentoRequest(
        @NotNull(message = "Aluno é obrigatório") Long alunoId,
        @NotNull(message = "Profissional é obrigatório") Long profissionalId,
        @NotNull(message = "Área é obrigatória") Long areaId,
        Long horarioId,
        @NotNull(message = "Data/hora de início é obrigatória") LocalDateTime dataHoraInicio,
        @NotNull(message = "Data/hora de fim é obrigatória") LocalDateTime dataHoraFim,
        @NotBlank(message = "Modalidade é obrigatória") String modalidade,
        String local
) {}