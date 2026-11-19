package com.br.unifor.paponline.dto;

import jakarta.validation.constraints.NotBlank;

public record BloqueioRequest(
        @NotBlank(message = "Informe o motivo do bloqueio") String motivo
) {}