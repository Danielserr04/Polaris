package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Modelo puro. Sin anotaciones de persistencia: el mapeo vive en MetaAhorroEntity.
 *
 * <p>Una cantidad que se quiere juntar, opcionalmente para una fecha. Lo
 * ahorrado ({@code importeActual}) no se guarda: es la suma de sus
 * AportacionMeta y lo rellena el adaptador al leer. Ver
 * docs/decisiones/036-meta-ahorro-con-aportaciones.md.
 *
 * <p>{@code diasRestantes} y {@code ahorroMensualNecesario} dependen de hoy:
 * los calcula {@link #calcularPlazo(LocalDate)}, que llama el servicio antes
 * de devolver la meta. El resto de derivados son getters sin estado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaAhorro {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private Long id;
    private Long usuarioId;
    private String nombre;
    private BigDecimal importeObjetivo;
    /** Opcional. Sin fecha no hay ahorro mensual necesario. */
    private LocalDate fechaLimite;
    /** Hex #RRGGBB. Opcional. */
    private String color;
    /** Nombre del icono en el frontend. Opcional. */
    private String icono;
    private Instant creadaEn;

    /** Suma de las aportaciones. Solo en lecturas; nunca baja de 0. */
    private BigDecimal importeActual;
    /** Dias hasta fechaLimite (negativo si ya paso). Nulo sin fecha. */
    private Long diasRestantes;
    /** Lo que habria que apartar cada mes para llegar a tiempo. Nulo sin fecha o si ya esta completada. */
    private BigDecimal ahorroMensualNecesario;

    public BigDecimal actual() {
        return importeActual == null ? BigDecimal.ZERO : importeActual;
    }

    public boolean isCompletada() {
        return actual().compareTo(importeObjetivo) >= 0;
    }

    /** Lo que falta para el objetivo; 0 si ya se alcanzo o se paso. */
    public BigDecimal getRestante() {
        return importeObjetivo.subtract(actual()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Porcentaje con un decimal, truncado: 99,99 % sale 99,9 y no 100 si aun
     * falta un centimo. Puede pasar de 100 si se ahorro de mas.
     */
    public BigDecimal getPorcentaje() {
        return actual().multiply(CIEN).divide(importeObjetivo, 1, RoundingMode.DOWN);
    }

    /**
     * Rellena diasRestantes y ahorroMensualNecesario respecto a {@code hoy}.
     *
     * <p>Los meses que quedan cuentan el mes en curso aunque este empezado:
     * del 2 de octubre al 31 de diciembre son 3 (octubre, noviembre y
     * diciembre). Si la fecha ya llego o paso, lo necesario es todo lo que
     * falta. Se redondea hacia arriba al centimo para no quedarse corto.
     */
    public void calcularPlazo(LocalDate hoy) {
        if (fechaLimite == null) {
            diasRestantes = null;
            ahorroMensualNecesario = null;
            return;
        }
        diasRestantes = ChronoUnit.DAYS.between(hoy, fechaLimite);
        if (isCompletada()) {
            ahorroMensualNecesario = null;
            return;
        }
        long meses = mesesHasta(hoy, fechaLimite);
        ahorroMensualNecesario = getRestante().divide(BigDecimal.valueOf(meses), 2, RoundingMode.UP);
    }

    private static long mesesHasta(LocalDate hoy, LocalDate limite) {
        if (!limite.isAfter(hoy)) {
            return 1;
        }
        long meses = ChronoUnit.MONTHS.between(hoy, limite);
        if (hoy.plusMonths(meses).isBefore(limite)) {
            meses++;
        }
        return Math.max(1, meses);
    }
}
