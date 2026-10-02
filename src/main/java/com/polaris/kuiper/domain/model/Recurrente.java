package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en RecurrenteEntity.
 *
 * <p>Un cargo que se repite. Cada vez que {@code proximaFecha} llega, el job
 * crea un Movimiento con estos datos y la avanza un periodo. {@code fechaInicio}
 * es el ancla: el dia del mes (o del año) en el que se cobra.
 *
 * <p>{@code cuotasTotal} nulo es "sin fin". Con valor, es un pago a plazos: al
 * pagar la ultima cuota se desactiva solo.
 *
 * <p>{@code categoria} y {@code cuenta} son las fichas completas, solo en
 * lecturas, igual que en Movimiento.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recurrente {

    private Long id;
    private Long usuarioId;
    private String concepto;
    private BigDecimal importe;
    private TipoMovimiento tipo;
    private Long categoriaId;
    private Categoria categoria;
    private String metodoPago;
    private FrecuenciaRecurrente frecuencia;
    private LocalDate fechaInicio;
    private LocalDate proximaFecha;
    private Integer cuotasTotal;
    private int cuotasPagadas;
    private boolean activo;
    /** Opcional: los movimientos que genera heredan esta cuenta. */
    private Long cuentaId;
    private Cuenta cuenta;

    /**
     * El cargo que sigue a {@code fecha}, con el dia de {@code fechaInicio}
     * cuando el mes lo tiene: un recibo del 31 cae el 30 en abril, el 28 en
     * febrero y vuelve al 31 en marzo.
     */
    public LocalDate siguienteDespuesDe(LocalDate fecha) {
        return switch (frecuencia) {
            case SEMANAL -> fecha.plusWeeks(1);
            case MENSUAL -> {
                LocalDate mes = fecha.plusMonths(1);
                yield mes.withDayOfMonth(Math.min(fechaInicio.getDayOfMonth(), mes.lengthOfMonth()));
            }
            case ANUAL -> {
                LocalDate anio = fecha.plusYears(1).withMonth(fechaInicio.getMonthValue());
                yield anio.withDayOfMonth(Math.min(fechaInicio.getDayOfMonth(), anio.lengthOfMonth()));
            }
        };
    }

    /** El primer cargo en {@code dia} o despues, contando desde fechaInicio. */
    public LocalDate primeroDesde(LocalDate dia) {
        LocalDate fecha = fechaInicio;
        while (fecha.isBefore(dia)) {
            fecha = siguienteDespuesDe(fecha);
        }
        return fecha;
    }

    public boolean plazosTerminados() {
        return cuotasTotal != null && cuotasPagadas >= cuotasTotal;
    }
}
