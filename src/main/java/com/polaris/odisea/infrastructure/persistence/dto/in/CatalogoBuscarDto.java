package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.TipoContenido;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Lo que llega por query params al buscar en el catalogo externo. El tipo es
 * obligatorio: cada fuente sabe de unos tipos y no de otros.
 */
public record CatalogoBuscarDto(
        @NotBlank(message = "no puede estar vacio")
        @Parameter(description = "Texto a buscar: titulo (o titulo y autor, en libros)", example = "dune")
        String q,

        @NotNull(message = "es obligatorio")
        @Parameter(description = "Tipo de contenido; decide la fuente: PELICULA y SERIE en TMDB, JUEGO en IGDB, LIBRO en "
                + "Open Library")
        TipoContenido tipo
) {
}
