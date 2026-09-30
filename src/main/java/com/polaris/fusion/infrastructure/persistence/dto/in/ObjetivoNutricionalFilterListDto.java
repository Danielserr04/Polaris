package com.polaris.fusion.infrastructure.persistence.dto.in;

import java.time.LocalDate;

/**
 * El dia en que se quiere el objetivo vigente, por query param:
 * {@code ?fecha=2026-03-15}. Opcional, por defecto hoy. No es un filtro de
 * listado: el historico devuelve siempre todas las filas.
 */
public record ObjetivoNutricionalFilterListDto(
        LocalDate fecha
) {
}
