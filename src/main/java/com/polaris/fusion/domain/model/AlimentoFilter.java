package com.polaris.fusion.domain.model;

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
public class AlimentoFilter {

    /** Coincidencia parcial contra nombre y marca, sin distinguir mayusculas. */
    private String texto;
}
