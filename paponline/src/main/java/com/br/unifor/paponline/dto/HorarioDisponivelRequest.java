package com.br.unifor.paponline.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HorarioDisponivelRequest(
        @NotNull(message = "Profissional é obrigatório") Long profissionalId,
        @NotNull(message = "Data é obrigatória") LocalDate data,
        @NotNull(message = "Hora de início é obrigatória") LocalTime horaInicio,
        @NotNull(message = "Hora de fim é obrigatória") LocalTime horaFim
) {}