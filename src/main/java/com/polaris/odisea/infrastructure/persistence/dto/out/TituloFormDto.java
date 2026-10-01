package com.polaris.odisea.infrastructure.persistence.dto.out;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * La ficha completa que devuelve el detalle.
 */
public record TituloFormDto(
        Long id,
        TipoContenido tipo,
        String titulo,
        String tituloOriginal,
        Integer anio,
        String sinopsis,
        String imagenUrl,
        String generos,
        @Schema(description = "Minutos en una pelicula; paginas en un libro; null en juegos")
        Integer duracionMin,
        FuenteExterna fuenteExterna,
        String idExterno
) {
}
