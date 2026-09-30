package com.polaris.atlas.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un punto de la progresion: un ejercicio en una sesion. {@code volumen} es
 * reps por peso sumado (0 con el peso corporal); todo con dos decimales.
 */
public record ProgresionSesionDto(
        Long sesionId,
        LocalDate fecha,
        BigDecimal volumen,
        int numeroSeries,
        BigDecimal pesoMaximo,
        long repsTotales
) {
}
