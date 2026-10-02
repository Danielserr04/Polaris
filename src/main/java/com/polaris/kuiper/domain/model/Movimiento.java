package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en MovimientoEntity.
 *
 * <p>{@code importe} es siempre positivo: el signo lo pone {@code tipo}.
 *
 * <p>{@code categoriaId} es el FK y siempre esta presente. {@code categoria} es
 * la ficha completa, enriquecida solo en lecturas (MovimientoJpaAdapter la
 * rellena al mapear desde la Entity); al crear o actualizar llega nula, porque
 * el cliente solo manda el id.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    private Long id;
    private Long usuarioId;
    private LocalDate fecha;
    private BigDecimal importe;
    private TipoMovimiento tipo;
    private Long categoriaId;
    private Categoria categoria;
    private String concepto;
    private String metodoPago;
    private boolean recurrente;
    /** Opcional: nulo es "sin cuenta". Ver docs/decisiones/039-cuentas-y-transferencias.md. */
    private Long cuentaId;
    private Cuenta cuenta;
}
