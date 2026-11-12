package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profissional")
@DiscriminatorValue("PROFISSIONAL")
@Getter
@Setter
@NoArgsConstructor
public class Profissional extends Usuario {

    @Column(nullable = false, unique = true)
    private String crp;

    @Column(nullable = false)
    private String cargo;
}