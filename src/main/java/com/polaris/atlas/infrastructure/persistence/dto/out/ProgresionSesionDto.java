package com.polaris.atlas.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un punto de la progresion: un ejercicio en una sesion. {@code volumen} es
 * reps por peso sumado (0 con el peso corporal); todo con dos decimales.
 */
public record ProgresionSesionDto(
        Long sesionId,
        LocalDate fecha,
        @Schema(description = "Suma de repeticiones por peso en la sesion, con 2 decimales; 0 con peso corporal")
        BigDecimal volumen,
        @Schema(description = "Series del ejercicio en la sesion")
        int numeroSeries,
        @Schema(description = "Mayor peso, en kg, de una serie de la sesion")
        BigDecimal pesoMaximo,
        @Schema(description = "Suma de repeticiones de todas las series")
        long repsTotales
) {
}
