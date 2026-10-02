package com.polaris.kuiper.infrastructure.persistence;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 * Sin coleccion de aportaciones: se leen y se suman por consulta, y el
 * borrado en cascada lo hace la FK de V18__kuiper_meta_ahorro.sql.
 */
@Entity
@Table(name = "meta_ahorro")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaAhorroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "importe_objetivo", nullable = false, precision = 10, scale = 2)
    private BigDecimal importeObjetivo;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(length = 7)
    private String color;

    @Column(length = 50)
    private String icono;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private Instant creadaEn;
}
