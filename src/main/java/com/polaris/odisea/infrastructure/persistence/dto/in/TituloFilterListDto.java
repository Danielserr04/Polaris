package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.TipoContenido;

/**
 * Los filtros que llegan por query params.
 */
public record TituloFilterListDto(
        TipoContenido tipo,
        String texto
) {
}
