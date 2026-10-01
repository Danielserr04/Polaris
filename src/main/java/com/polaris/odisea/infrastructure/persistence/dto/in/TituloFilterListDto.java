package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.TipoContenido;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params.
 */
public record TituloFilterListDto(
        @Parameter(description = "Tipo de contenido")
        TipoContenido tipo,
        @Parameter(description = "Texto que debe contener el titulo o el titulo original, sin distinguir mayusculas",
                example = "dune")
        String texto
) {
}
