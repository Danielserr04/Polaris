package com.polaris.atlas.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 *
 * <p>usuario_id es nullable a proposito: NULL es una fila del catalogo
 * compartido. El unique (usuario_id, nombre) de los ejercicios propios vive en
 * V12__atlas_ejercicio.sql.
 */
@Entity
@Table(name = "ejercicio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EjercicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "grupo_muscular", nullable = false, length = 50)
    private String grupoMuscular;

    @Column(length = 100)
    private String equipamiento;
}
