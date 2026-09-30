package com.polaris.fusion.infrastructure.persistence.dto.in;

import com.polaris.fusion.domain.model.MomentoComida;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?fecha=2026-09-30} (dia exacto), {@code ?desde=&hasta=} (rango
 * inclusivo) y {@code ?momento=CENA}.
 */
public record ComidaFilterListDto(
        LocalDate fecha,
        LocalDate desde,
        LocalDate hasta,
        MomentoComida momento
) {
}
