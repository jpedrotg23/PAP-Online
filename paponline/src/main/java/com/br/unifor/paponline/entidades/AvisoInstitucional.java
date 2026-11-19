package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "avisos_institucionais")
@Getter
@Setter
@NoArgsConstructor
public class AvisoInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String conteudo;

    @Column(nullable = false)
    private String categoria; // EVENTO, CAMPANHA, AVISO, MANUTENCAO, REUNIAO, CAPACITACAO

    private Boolean destaque = false;

    @Column(nullable = false)
    private String publicoAlvo; // TODOS, ALUNOS, PROFISSIONAIS

    @Column(nullable = false)
    private String status; // RASCUNHO, AGENDADO, PUBLICADO, EXPIRADO

    private LocalDateTime criadoEm;

    private LocalDateTime publicadoEm;
}