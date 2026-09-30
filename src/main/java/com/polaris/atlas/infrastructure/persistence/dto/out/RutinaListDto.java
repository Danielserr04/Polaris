package com.polaris.atlas.infrastructure.persistence.dto.out;

/**
 * La version ligera para el listado: sin lineas, con cuantos ejercicios lleva.
 */
public record RutinaListDto(
        Long id,
        String nombre,
        String descripcion,
        boolean activa,
        int numeroEjercicios
) {
}
