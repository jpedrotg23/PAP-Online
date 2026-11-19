package com.br.unifor.paponline.entidades;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class Profissional extends Usuario {

    private String crp;
    private String cargo;
    private Long areaId;
}