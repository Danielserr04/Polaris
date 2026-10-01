package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params:
 * {@code ?ejercicioId=3&desde=2026-09-01&hasta=2026-09-30}. El ejercicio es
 * obligatorio; el rango de fechas (inclusivo) es opcional.
 */
public record ProgresionFilterListDto(
        @NotNull(message = "es obligatorio")
        @Parameter(description = "Id del ejercicio (obligatorio): del catalogo o propio")
        Long ejercicioId,
        @Parameter(description = "Fecha minima, inclusive (yyyy-MM-dd)")
        LocalDate desde,
        @Parameter(description = "Fecha maxima, inclusive (yyyy-MM-dd)")
        LocalDate hasta
) {
}
