package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?desde=2026-09-01&hasta=2026-09-30} (rango inclusivo) y
 * {@code ?rutinaId=3}.
 */
public record SesionFilterListDto(
        @Parameter(description = "Fecha minima, inclusive (yyyy-MM-dd)", example = "2026-09-01")
        LocalDate desde,
        @Parameter(description = "Fecha maxima, inclusive (yyyy-MM-dd)", example = "2026-09-30")
        LocalDate hasta,
        @Parameter(description = "Id de la rutina con la que se hicieron")
        Long rutinaId
) {
}
