package com.polaris.nucleo.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params: rango de fechas inclusivo,
 * {@code ?desde=2026-01-01&hasta=2026-01-31}.
 */
public record RegistroPesoFilterListDto(
        @Parameter(description = "Fecha minima, inclusive (yyyy-MM-dd)", example = "2026-01-01")
        LocalDate desde,
        @Parameter(description = "Fecha maxima, inclusive (yyyy-MM-dd)", example = "2026-01-31")
        LocalDate hasta
) {
}
