package com.polaris.atlas.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * La ficha completa que devuelve el detalle. Sin usuarioId: esPropio dice si es
 * del usuario (editable) o del catalogo (solo lectura).
 */
public record EjercicioFormDto(
        Long id,
        String nombre,
        String grupoMuscular,
        String equipamiento,
        @Schema(description = "true si es tuyo (editable); false si es del catalogo compartido (solo lectura)")
        boolean esPropio
) {
}
