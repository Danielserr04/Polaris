package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos, no como Specification.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EjercicioFilter {

    /** Coincidencia exacta, sin distinguir mayusculas ni tildes (collation de la columna). */
    private String grupoMuscular;

    /** Coincidencia parcial contra el nombre, sin distinguir mayusculas. */
    private String texto;
}
