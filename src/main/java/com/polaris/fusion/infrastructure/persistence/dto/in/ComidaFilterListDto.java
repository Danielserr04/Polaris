package com.polaris.fusion.infrastructure.persistence.dto.in;

import com.polaris.fusion.domain.model.MomentoComida;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?fecha=2026-09-30} (dia exacto), {@code ?desde=&hasta=} (rango
 * inclusivo) y {@code ?momento=CENA}.
 */
public record ComidaFilterListDto(
        @Parameter(description = "Dia exacto (yyyy-MM-dd)", example = "2026-09-30")
        LocalDate fecha,
        @Parameter(description = "Fecha minima, inclusive (yyyy-MM-dd)")
        LocalDate desde,
        @Parameter(description = "Fecha maxima, inclusive (yyyy-MM-dd)")
        LocalDate hasta,
        @Parameter(description = "Momento del dia: DESAYUNO, COMIDA, CENA o SNACK")
        MomentoComida momento
) {
}
