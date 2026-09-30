package com.polaris.atlas.infrastructure.persistence.dto.out;

/**
 * La ficha completa que devuelve el detalle. Sin usuarioId: esPropio dice si es
 * del usuario (editable) o del catalogo (solo lectura).
 */
public record EjercicioFormDto(
        Long id,
        String nombre,
        String grupoMuscular,
        String equipamiento,
        boolean esPropio
) {
}
