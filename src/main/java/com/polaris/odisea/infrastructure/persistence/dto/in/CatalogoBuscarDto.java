package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.TipoContenido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Lo que llega por query params al buscar en el catalogo externo. El tipo es
 * obligatorio: cada fuente sabe de unos tipos y no de otros.
 */
public record CatalogoBuscarDto(
        @NotBlank(message = "no puede estar vacio")
        String q,

        @NotNull(message = "es obligatorio")
        TipoContenido tipo
) {
}
