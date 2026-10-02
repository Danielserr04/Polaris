package com.polaris.kuiper.infrastructure.persistence;

import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mapeo JPA. Las anotaciones de persistencia viven aqui y solo aqui.
 * {@code @ManyToOne} EAGER por lo mismo que en MovimientoEntity.
 */
@Entity
@Table(name = "recurrente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String concepto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrecuenciaRecurrente frecuencia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "proxima_fecha", nullable = false)
    private LocalDate proximaFecha;

    @Column(name = "cuotas_total")
    private Integer cuotasTotal;

    @Column(name = "cuotas_pagadas", nullable = false)
    private int cuotasPagadas;

    @Column(nullable = false)
    private boolean activo;

    /** Opcional: nula es "sin cuenta". EAGER por lo mismo que la categoria. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cuenta_id")
    private CuentaEntity cuenta;
}
