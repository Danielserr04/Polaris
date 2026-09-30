package com.polaris.atlas.infrastructure.persistence.dto.in;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?desde=2026-09-01&hasta=2026-09-30} (rango inclusivo) y
 * {@code ?rutinaId=3}.
 */
public record SesionFilterListDto(
        LocalDate desde,
        LocalDate hasta,
        Long rutinaId
) {
}
