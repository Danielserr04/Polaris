package com.polaris.atlas.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * La version ligera para el listado. Ejercicio ya es pequeno, asi que hoy
 * coincide con el FormDto; se mantienen separados por la plantilla.
 */
public record EjercicioListDto(
        Long id,
        String nombre,
        String grupoMuscular,
        String equipamiento,
        @Schema(description = "true si es tuyo (editable); false si es del catalogo compartido (solo lectura)")
        boolean esPropio
) {
}
