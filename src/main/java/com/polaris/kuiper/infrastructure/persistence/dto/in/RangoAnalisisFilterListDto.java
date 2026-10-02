package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * Rango inclusivo por query param: {@code ?desde=2026-09-01&hasta=2026-09-30}.
 * Sin {@code desde}, el dia 1 del mes actual; sin {@code hasta}, el ultimo dia
 * del mes actual. {@code limite} solo lo usa el top de comercios.
 */
public record RangoAnalisisFilterListDto(
        @Parameter(description = "Primer dia, formato yyyy-MM-dd; por defecto el 1 del mes actual",
                example = "2026-09-01")
        LocalDate desde,
        @Parameter(description = "Ultimo dia, formato yyyy-MM-dd; por defecto el ultimo del mes actual",
                example = "2026-09-30")
        LocalDate hasta,
        @Parameter(description = "Solo en /comercios: cuantos devolver, de 1 a 50; por defecto 10", example = "10")
        Integer limite
) {
}
