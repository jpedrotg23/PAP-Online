package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aluno")
@DiscriminatorValue("ALUNO")
@Getter
@Setter
@NoArgsConstructor
public class Aluno extends Usuario {

    @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false)
    private String curso;

    private String vinculo;
}