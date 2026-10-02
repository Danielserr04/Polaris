package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Un mes de la serie de evolucion. Se calcula al vuelo, nunca se guarda.
 * {@code tasaAhorro} es balance entre ingresos en porcentaje; nula si el mes
 * no tiene ingresos (no hay sobre que calcularla).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvolucionMensual {

    private YearMonth periodo;
    private BigDecimal ingresos;
    private BigDecimal gastos;
    private BigDecimal balance;
    private BigDecimal tasaAhorro;
}
