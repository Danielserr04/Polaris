package com.polaris.atlas.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params:
 * {@code ?ejercicioId=3&desde=2026-09-01&hasta=2026-09-30}. El ejercicio es
 * obligatorio; el rango de fechas (inclusivo) es opcional.
 */
public record ProgresionFilterListDto(
        @NotNull(message = "es obligatorio")
        Long ejercicioId,
        LocalDate desde,
        LocalDate hasta
) {
}
