package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * El gasto previsto a fin de mes: lo ya gastado, mas el ritmo diario del gasto
 * no recurrente por los dias que faltan, mas los recurrentes que quedan por
 * cobrar. Los recurrentes ya cobrados no se extrapolan: son fijos.
 *
 * <p>En un mes CERRADO la proyeccion es el gasto real. En uno FUTURO solo
 * cuentan los recurrentes. {@code presupuestoMensual} es la suma de los
 * presupuestos MENSUAL, nula si no hay ninguno.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyeccionMensual {

    private YearMonth periodo;
    private EstadoProyeccion estado;
    private int diasMes;
    private int diasTranscurridos;
    private BigDecimal gastoActual;
    private BigDecimal gastoVariable;
    private BigDecimal ritmoDiario;
    private BigDecimal proyeccionVariable;
    private BigDecimal recurrentesPendientes;
    private int cargosPendientes;
    private BigDecimal gastoProyectado;
    private BigDecimal presupuestoMensual;
}
