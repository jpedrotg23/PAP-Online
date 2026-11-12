package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "areas_servico")
@Getter
@Setter
@NoArgsConstructor
public class AreaServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome; // PSICOLOGIA, PSICOPEDAGOGIA

    private String descricao;
}