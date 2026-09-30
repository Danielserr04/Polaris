package com.polaris.atlas.infrastructure.persistence.dto.out;

/**
 * La version ligera para el listado. Ejercicio ya es pequeno, asi que hoy
 * coincide con el FormDto; se mantienen separados por la plantilla.
 */
public record EjercicioListDto(
        Long id,
        String nombre,
        String grupoMuscular,
        String equipamiento,
        boolean esPropio
) {
}
