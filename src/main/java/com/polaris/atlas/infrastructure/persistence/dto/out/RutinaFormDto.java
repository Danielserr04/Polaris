package com.polaris.atlas.infrastructure.persistence.dto.out;

import java.util.List;

/**
 * La ficha completa que devuelve el detalle: la rutina con sus lineas, ya
 * ordenadas por orden. Sin usuarioId.
 */
public record RutinaFormDto(
        Long id,
        String nombre,
        String descripcion,
        boolean activa,
        List<RutinaEjercicioFormDto> lineas
) {
}
