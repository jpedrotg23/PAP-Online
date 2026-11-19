package com.br.unifor.paponline.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificacaoRequest(
        @NotNull(message = "Usuário é obrigatório") Long usuarioId,
        @NotBlank(message = "Título é obrigatório") String titulo,
        @NotBlank(message = "Mensagem é obrigatória") String mensagem
) {}