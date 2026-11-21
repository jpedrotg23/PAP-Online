package com.br.unifor.paponline.dto;

import com.br.unifor.paponline.entidades.Aluno;

public record AlunoResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String fotoUrl,
        Boolean ativo,
        String matricula,
        String curso,
        String vinculo
) {
    public static AlunoResponse from(Aluno aluno) {
        return new AlunoResponse(aluno.getId(), aluno.getNome(), aluno.getEmail(),
                aluno.getTelefone(), aluno.getFotoUrl(), aluno.getAtivo(),
                aluno.getMatricula(), aluno.getCurso(), aluno.getVinculo());
    }
}
