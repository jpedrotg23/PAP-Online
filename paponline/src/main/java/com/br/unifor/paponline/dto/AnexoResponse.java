package com.br.unifor.paponline.dto;

import com.br.unifor.paponline.entidades.Anexo;

public record AnexoResponse(
        Long id,
        Long solicitacaoId,
        String nomeArquivo,
        String tipoMime,
        Long tamanho
) {
    public static AnexoResponse from(Anexo anexo) {
        return new AnexoResponse(anexo.getId(), anexo.getSolicitacaoId(), anexo.getNomeArquivo(),
                anexo.getTipoMime(), anexo.getTamanho());
    }
}
