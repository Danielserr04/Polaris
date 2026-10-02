package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params: {@code ?desde=2026-09-01&hasta=2026-09-30}.
 * Rango de fechas inclusivo, opcional.
 */
public record TrabajoMuscularFilterListDto(
        @Parameter(description = "Fecha minima, inclusive (yyyy-MM-dd)")
        LocalDate desde,
        @Parameter(description = "Fecha maxima, inclusive (yyyy-MM-dd)")
        LocalDate hasta
) {
}
