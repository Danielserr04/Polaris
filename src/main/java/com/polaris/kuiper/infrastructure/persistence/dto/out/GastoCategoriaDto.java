package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Lo gastado en una categoria contra su presupuesto mensual. limiteMensual y
 * restante son nulos si no hay presupuesto; restante es negativo si se excedio.
 */
public record GastoCategoriaDto(
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        @Schema(description = "Gasto de la categoria en el mes; 0.00 si solo tiene presupuesto")
        BigDecimal gastado,
        @Schema(description = "Presupuesto MENSUAL de la categoria; null si no tiene")
        BigDecimal limiteMensual,
        @Schema(description = "limiteMensual menos gastado; negativo si se excedio; null si no hay limite")
        BigDecimal restante
) {
}
