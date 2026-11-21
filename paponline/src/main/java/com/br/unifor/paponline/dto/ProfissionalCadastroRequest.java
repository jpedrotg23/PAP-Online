package com.br.unifor.paponline.dto;

import com.br.unifor.paponline.entidades.Profissional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfissionalCadastroRequest(
        @NotBlank(message = "Nome é obrigatório.")
        String nome,
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email,
        @NotBlank(message = "Telefone é obrigatório.")
        String telefone,
        @NotBlank(message = "Senha é obrigatória.")
        String senha,
        String crp,
        String cargo,
        Long areaId
) {
    public ProfissionalCadastroRequest {
        email = email == null ? null : email.trim();
    }

    public Profissional toEntity() {
        Profissional profissional = new Profissional();
        profissional.setNome(nome);
        profissional.setEmail(email);
        profissional.setTelefone(telefone);
        profissional.setSenha(senha);
        profissional.setCrp(crp);
        profissional.setCargo(cargo);
        profissional.setAreaId(areaId);
        return profissional;
    }

    @Override
    public String toString() {
        return "ProfissionalCadastroRequest[senha=<redacted>]";
    }
}
