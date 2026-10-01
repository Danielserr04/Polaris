package com.polaris.atlas.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Una serie del detalle, con el nombre y el grupo muscular del ejercicio.
 */
public record SerieRegistroFormDto(
        Long id,
        Long ejercicioId,
        String ejercicioNombre,
        String ejercicioGrupoMuscular,
        Integer numeroSerie,
        Integer reps,
        @Schema(description = "Peso en kg; 0 si fue con el peso corporal")
        BigDecimal pesoKg,
        @Schema(description = "Esfuerzo percibido, de 1 a 10; null si no se anoto")
        BigDecimal rpe
) {
}
