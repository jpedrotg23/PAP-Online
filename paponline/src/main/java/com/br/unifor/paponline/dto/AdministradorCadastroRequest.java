package com.br.unifor.paponline.dto;

import com.br.unifor.paponline.entidades.Administrador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdministradorCadastroRequest(
        @NotBlank(message = "Nome é obrigatório.")
        String nome,
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email,
        String telefone,
        @NotBlank(message = "Senha é obrigatória.")
        String senha,
        @NotBlank(message = "Cargo é obrigatório.")
        String cargo
) {
    public AdministradorCadastroRequest {
        email = email == null ? null : email.trim();
    }

    public Administrador toEntity() {
        Administrador administrador = new Administrador();
        administrador.setNome(nome);
        administrador.setEmail(email);
        administrador.setTelefone(telefone);
        administrador.setSenha(senha);
        administrador.setCargo(cargo);
        return administrador;
    }

    @Override
    public String toString() {
        return "AdministradorCadastroRequest[senha=<redacted>]";
    }
}
