package com.br.unifor.paponline.dto;

import com.br.unifor.paponline.entidades.Aluno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlunoCadastroRequest(
        @NotBlank(message = "Nome é obrigatório.")
        String nome,
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email,
        @NotBlank(message = "Telefone é obrigatório.")
        String telefone,
        @NotBlank(message = "Senha é obrigatória.")
        String senha,
        @NotBlank(message = "Matrícula é obrigatória.")
        String matricula,
        @NotBlank(message = "Curso é obrigatório.")
        String curso,
        String vinculo,
        @NotNull(message = "É necessário informar o aceite do termo de consentimento.")
        Boolean consentimentoAceito
) {
    public AlunoCadastroRequest {
        email = email == null ? null : email.trim();
    }

    public Aluno toEntity() {
        Aluno aluno = new Aluno();
        aluno.setNome(nome);
        aluno.setEmail(email);
        aluno.setTelefone(telefone);
        aluno.setSenha(senha);
        aluno.setMatricula(matricula);
        aluno.setCurso(curso);
        aluno.setVinculo(vinculo);
        aluno.setConsentimentoAceito(consentimentoAceito);
        return aluno;
    }

    @Override
    public String toString() {
        return "AlunoCadastroRequest[senha=<redacted>]";
    }
}
