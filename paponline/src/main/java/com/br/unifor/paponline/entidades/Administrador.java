package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class Administrador extends Usuario {

    @Column(nullable = false)
    private String cargo;
}