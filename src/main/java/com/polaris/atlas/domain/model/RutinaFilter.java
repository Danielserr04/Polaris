package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos, no como Specification. {@code activa}
 * nulo significa "todas".
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutinaFilter {

    private Boolean activa;
}
