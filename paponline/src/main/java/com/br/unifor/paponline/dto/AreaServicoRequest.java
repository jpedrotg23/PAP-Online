package com.br.unifor.paponline.dto;

import jakarta.validation.constraints.NotBlank;

public record AreaServicoRequest(
        @NotBlank(message = "Nome da área é obrigatório") String nome,
        String descricao
) {}