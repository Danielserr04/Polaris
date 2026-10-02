package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * El gasto de una categoria en el rango frente al rango anterior.
 */
public record CategoriaComparadaDto(
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        @Schema(description = "Gasto en el rango; 0.00 si solo gasto en el anterior")
        BigDecimal gastado,
        @Schema(description = "Peso sobre el gasto total del rango, en porcentaje", example = "32.10")
        BigDecimal porcentaje,
        @Schema(description = "Gasto en el rango anterior")
        BigDecimal gastadoAnterior,
        @Schema(description = "gastado menos gastadoAnterior; negativo si bajo")
        BigDecimal diferencia,
        @Schema(description = "diferencia sobre gastadoAnterior, en porcentaje; null si antes no hubo gasto")
        BigDecimal variacion
) {
}
