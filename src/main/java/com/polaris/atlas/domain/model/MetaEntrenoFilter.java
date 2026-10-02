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
public class MetaEntrenoFilter {

    private TipoMetaEntreno tipo;
}
