package com.polaris.atlas.infrastructure.persistence.dto.out;

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
        BigDecimal pesoKg,
        BigDecimal rpe
) {
}
