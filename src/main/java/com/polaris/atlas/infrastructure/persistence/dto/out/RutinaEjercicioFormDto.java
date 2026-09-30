package com.polaris.atlas.infrastructure.persistence.dto.out;

/**
 * Una linea del detalle, con el nombre y el grupo muscular del ejercicio.
 */
public record RutinaEjercicioFormDto(
        Long id,
        Long ejercicioId,
        String ejercicioNombre,
        String ejercicioGrupoMuscular,
        Integer orden,
        Integer seriesObjetivo,
        String repsObjetivo
) {
}
