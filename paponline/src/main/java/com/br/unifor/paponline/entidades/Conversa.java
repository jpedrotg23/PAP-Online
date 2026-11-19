package com.br.unifor.paponline.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversas")
@Getter
@Setter
@NoArgsConstructor
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null em conversas profissional↔coordenação. */
    private Long alunoId;

    /** Null em conversas aluno↔coordenação. */
    private Long profissionalId;

    /** Null em conversas aluno↔profissional. */
    private Long adminId;

    private LocalDateTime criadoEm = LocalDateTime.now();
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}