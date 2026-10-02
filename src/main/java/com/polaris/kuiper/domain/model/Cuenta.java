package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en CuentaEntity.
 *
 * <p>{@code saldoInicial} lleva signo: una tarjeta de credito puede empezar en
 * negativo. {@code saldoActual} no se guarda: lo calcula CuentaService como
 * saldoInicial + ingresos - gastos de sus movimientos + transferencias
 * entrantes - salientes. Solo viene relleno en lo que devuelve CuentaService;
 * dentro de un Movimiento o una Transferencia queda nulo.
 *
 * <p>Archivar no borra: la cuenta deja de ofrecerse para movimientos nuevos,
 * pero conserva su historico y su saldo.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta {

    private Long id;
    private Long usuarioId;
    private String nombre;
    private TipoCuenta tipo;
    private BigDecimal saldoInicial;
    /** Hex #RRGGBB. Opcional. */
    private String color;
    /** Nombre del icono en el frontend. Opcional. */
    private String icono;
    /** Entidad bancaria. Opcional. */
    private String banco;
    private boolean archivada;
    private BigDecimal saldoActual;
}
