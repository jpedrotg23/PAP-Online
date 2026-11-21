package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String telefone;
    @JsonIgnore
    private String senha;
    @JsonIgnore
    private LocalDate dataSolicitacoesRecuperacao;
    @JsonIgnore
    private Integer solicitacoesRecuperacaoHoje = 0;
    @JsonIgnore
    private LocalDateTime ultimaSolicitacaoRecuperacao;
    private String fotoUrl;

    private Boolean ativo = true;

    private LocalDateTime dataCadastro = LocalDateTime.now();
}