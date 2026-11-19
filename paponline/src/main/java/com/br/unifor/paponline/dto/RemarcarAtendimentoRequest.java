package com.br.unifor.paponline.dto;

import jakarta.validation.constraints.NotNull;

public record RemarcarAtendimentoRequest(
        @NotNull(message = "Informe o novo horário para a remarcação.")
        Long horarioId
) {
}
