package com.polaris.kuiper.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Como va una categoria frente a su presupuesto. Ver
 * docs/decisiones/035-presupuesto-umbral-de-alerta.md.
 *
 * <ul>
 *   <li>{@code SIN_PRESUPUESTO}: la categoria no tiene limite en ese periodo.</li>
 *   <li>{@code OK}: lo gastado no llega al umbral de alerta.</li>
 *   <li>{@code AVISO}: lo gastado llega al umbral (y, como mucho, iguala el limite).</li>
 *   <li>{@code EXCEDIDO}: lo gastado supera el limite.</li>
 * </ul>
 */
public enum EstadoPresupuesto {
    SIN_PRESUPUESTO,
    OK,
    AVISO,
    EXCEDIDO;

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    /**
     * Compara sin redondear (gastado * 100 frente a limite * umbral), para que
     * un 79,96 % no cuente como aviso por salir 80,0 al redondear.
     */
    public static EstadoPresupuesto de(BigDecimal gastado, BigDecimal limite, Integer porcentajeAlerta) {
        if (limite == null) {
            return SIN_PRESUPUESTO;
        }
        if (gastado.compareTo(limite) > 0) {
            return EXCEDIDO;
        }
        BigDecimal umbral = limite.multiply(BigDecimal.valueOf(porcentajeAlerta));
        return gastado.multiply(CIEN).compareTo(umbral) >= 0 ? AVISO : OK;
    }

    /** gastado / limite * 100 con un decimal; nulo sin limite (o con limite 0, que el DTO no deja guardar). */
    public static BigDecimal porcentaje(BigDecimal gastado, BigDecimal limite) {
        if (limite == null || limite.signum() == 0) {
            return null;
        }
        return gastado.multiply(CIEN).divide(limite, 1, RoundingMode.HALF_UP);
    }
}
