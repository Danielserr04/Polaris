package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Lo que se calcula al vuelo a partir de los movimientos del mes: nunca se
 * guarda un total. {@code balance} es ingresos menos gastos (puede ser negativo).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenMensual {

    private YearMonth periodo;
    private BigDecimal ingresos;
    private BigDecimal gastos;
    private BigDecimal balance;
    /** Mayor gasto primero. Incluye categorias con presupuesto mensual aunque no hayan gastado nada. */
    private List<GastoCategoria> gastoPorCategoria;
    /** Suma de los limites MENSUALES; 0.00 si no hay ninguno. */
    private BigDecimal presupuestoTotal;
    /** Categorias en AVISO (llegan al umbral sin pasarse). */
    private int categoriasEnAviso;
    /** Categorias EXCEDIDAS (gastado mayor que el limite). */
    private int categoriasExcedidas;
}
