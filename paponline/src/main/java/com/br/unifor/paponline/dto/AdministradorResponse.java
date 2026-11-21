package com.br.unifor.paponline.dto;

import com.br.unifor.paponline.entidades.Administrador;

public record AdministradorResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String fotoUrl,
        Boolean ativo,
        String cargo
) {
    public static AdministradorResponse from(Administrador administrador) {
        return new AdministradorResponse(administrador.getId(), administrador.getNome(),
                administrador.getEmail(), administrador.getTelefone(), administrador.getFotoUrl(),
                administrador.getAtivo(), administrador.getCargo());
    }
}
