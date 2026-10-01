package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDate;

/**
 * El dia en que se quiere el objetivo vigente, por query param:
 * {@code ?fecha=2026-03-15}. Opcional, por defecto hoy. No es un filtro de
 * listado: el historico devuelve siempre todas las filas.
 */
public record ObjetivoNutricionalFilterListDto(
        @Parameter(description = "Dia para el que se quiere el objetivo vigente (yyyy-MM-dd); por defecto hoy",
                example = "2026-03-15")
        LocalDate fecha
) {
}
