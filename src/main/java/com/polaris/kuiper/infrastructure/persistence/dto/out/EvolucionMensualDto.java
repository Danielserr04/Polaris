package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Un mes de la serie de evolucion. {@code periodo} viaja como texto {@code 2026-09}.
 */
public record EvolucionMensualDto(
        @Schema(description = "Mes, formato yyyy-MM", example = "2026-09")
        String periodo,
        BigDecimal ingresos,
        BigDecimal gastos,
        @Schema(description = "ingresos menos gastos; negativo si se gasto mas de lo ingresado")
        BigDecimal balance,
        @Schema(description = "balance entre ingresos, en porcentaje; null si el mes no tiene ingresos",
                example = "23.50")
        BigDecimal tasaAhorro
) {
}
