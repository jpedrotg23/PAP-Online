package com.br.unifor.paponline.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "anexos")
@Getter
@Setter
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long solicitacaoId;

    @Column(nullable = false, length = 255)
    private String nomeArquivo;

    @Column(nullable = false, length = 500)
    private String caminho;

    @Column(length = 120)
    private String tipoMime;

    private Long tamanho;
}