package com.br.unifor.paponline.dto;

import com.br.unifor.paponline.entidades.Profissional;

public record ProfissionalResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String fotoUrl,
        Boolean ativo,
        String crp,
        String cargo,
        Long areaId
) {
    public static ProfissionalResponse from(Profissional profissional) {
        return new ProfissionalResponse(profissional.getId(), profissional.getNome(),
                profissional.getEmail(), profissional.getTelefone(), profissional.getFotoUrl(),
                profissional.getAtivo(), profissional.getCrp(), profissional.getCargo(),
                profissional.getAreaId());
    }
}
