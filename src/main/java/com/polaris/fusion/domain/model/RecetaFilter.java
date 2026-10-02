package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos, no como Specification. {@code q} busca en
 * el nombre; opcional.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaFilter {

    private String q;
}
