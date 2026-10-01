package com.polaris.odisea.infrastructure.persistence.dto.out;

import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.TipoContenido;

/**
 * La version ligera para el listado. Aplanada, no anidada, como hace el resto
 * de DTOs de la plantilla: trae titulo y caratula porque sin eso no sirve de
 * mucho, pero no la sinopsis. Trae tambien titulo original, anio y duracion del titulo
 * (ya estaban cargados con la entrada: no cuestan una consulta mas) para no obligar al
 * cliente a pedir cada ficha.
 */
public record EntradaListDto(
        Long id,
        Long tituloId,
        String tituloTitulo,
        String tituloOriginal,
        Integer tituloAnio,
        Integer tituloDuracionMin,
        String tituloImagenUrl,
        TipoContenido tituloTipo,
        EstadoEntrada estado,
        Integer valoracion,
        boolean favorito,
        Integer progreso
) {
}
