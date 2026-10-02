package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?desde=2026-09-01&hasta=2026-09-30&cuentaId=3}.
 */
public record TransferenciaFilterListDto(
        @Parameter(description = "Fecha minima, inclusive (yyyy-MM-dd)", example = "2026-09-01")
        LocalDate desde,
        @Parameter(description = "Fecha maxima, inclusive (yyyy-MM-dd)", example = "2026-09-30")
        LocalDate hasta,
        @Parameter(description = "Id de una cuenta: casa como origen o como destino")
        Long cuentaId
) {
}
