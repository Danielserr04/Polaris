package com.polaris.odisea.infrastructure.persistence.dto.out;

import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.TipoContenido;

/**
 * La version ligera para el listado. Aplanada, no anidada, como hace el resto
 * de DTOs de la plantilla: trae titulo y caratula porque sin eso no sirve de
 * mucho, pero no la sinopsis.
 */
public record EntradaListDto(
        Long id,
        Long tituloId,
        String tituloTitulo,
        String tituloImagenUrl,
        TipoContenido tituloTipo,
        EstadoEntrada estado,
        Integer valoracion,
        boolean favorito,
        Integer progreso
) {
}
