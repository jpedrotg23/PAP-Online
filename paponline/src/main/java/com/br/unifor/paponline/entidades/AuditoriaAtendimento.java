package com.br.unifor.paponline.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_atendimentos")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long atendimentoId;

    private Long usuarioId;

    @Column(nullable = false, length = 20)
    private String perfil;

    @Column(nullable = false, length = 40)
    private String acao;

    @Column(nullable = false)
    private LocalDateTime ocorridoEm;

    @PreUpdate
    @PreRemove
    private void impedirAlteracao() {
        throw new IllegalStateException("Registros de auditoria são imutáveis.");
    }
}
