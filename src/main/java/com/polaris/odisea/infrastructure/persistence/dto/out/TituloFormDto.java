package com.polaris.odisea.infrastructure.persistence.dto.out;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;

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
        Integer duracionMin,
        FuenteExterna fuenteExterna,
        String idExterno
) {
}
