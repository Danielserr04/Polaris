package com.polaris.nucleo.infrastructure.persistence.dto.in;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params: rango de fechas inclusivo,
 * {@code ?desde=2026-01-01&hasta=2026-01-31}.
 */
public record RegistroPesoFilterListDto(
        LocalDate desde,
        LocalDate hasta
) {
}
