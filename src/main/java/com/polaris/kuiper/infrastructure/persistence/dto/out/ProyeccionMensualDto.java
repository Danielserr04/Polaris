package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Gasto previsto a fin de mes. gastoProyectado = gastoActual +
 * proyeccionVariable + recurrentesPendientes.
 */
public record ProyeccionMensualDto(
        @Schema(description = "Mes, formato yyyy-MM", example = "2026-09")
        String periodo,
        @Schema(description = "CERRADO (mes pasado: la proyeccion es el gasto real), EN_CURSO o FUTURO "
                + "(solo cuentan los recurrentes)")
        String estado,
        int diasMes,
        int diasTranscurridos,
        @Schema(description = "Gasto ya registrado en el mes")
        BigDecimal gastoActual,
        @Schema(description = "La parte de gastoActual que no viene de recurrentes")
        BigDecimal gastoVariable,
        @Schema(description = "gastoVariable entre los dias transcurridos")
        BigDecimal ritmoDiario,
        @Schema(description = "Lo que se gastaria al ritmo diario en los dias que faltan")
        BigDecimal proyeccionVariable,
        @Schema(description = "Recurrentes de gasto activos que quedan por cobrar en el mes")
        BigDecimal recurrentesPendientes,
        int cargosPendientes,
        BigDecimal gastoProyectado,
        @Schema(description = "Suma de los presupuestos MENSUAL; null si no hay ninguno")
        BigDecimal presupuestoMensual
) {
}
