package com.polaris.kuiper.domain.model;

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
public class CategoriaFilter {

    private TipoMovimiento tipo;
}
