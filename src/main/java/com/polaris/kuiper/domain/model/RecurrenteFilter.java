package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos, no como Specification. Todos opcionales.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurrenteFilter {

    private Boolean activo;
    private TipoMovimiento tipo;
    private Long categoriaId;
}
