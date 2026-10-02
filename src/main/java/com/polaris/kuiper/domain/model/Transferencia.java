package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en TransferenciaEntity.
 *
 * <p>Dinero que pasa de una cuenta propia a otra. No es ni ingreso ni gasto:
 * mueve saldo entre cuentas y no cuenta en el resumen mensual. {@code importe}
 * es siempre positivo; la direccion la dan origen y destino.
 *
 * <p>{@code cuentaOrigen} y {@code cuentaDestino} son las fichas completas,
 * solo en lecturas, igual que la categoria en Movimiento.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transferencia {

    private Long id;
    private Long usuarioId;
    private Long cuentaOrigenId;
    private Cuenta cuentaOrigen;
    private Long cuentaDestinoId;
    private Cuenta cuentaDestino;
    private BigDecimal importe;
    private LocalDate fecha;
    private String concepto;
}
