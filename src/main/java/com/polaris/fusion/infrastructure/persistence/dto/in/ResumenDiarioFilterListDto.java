package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * El dia a resumir, por query param: {@code ?fecha=2026-09-30}. Opcional, por
 * defecto hoy.
 */
public record ResumenDiarioFilterListDto(
        @Parameter(description = "Dia a resumir (yyyy-MM-dd); por defecto hoy", example = "2026-09-30")
        LocalDate fecha
) {
}
