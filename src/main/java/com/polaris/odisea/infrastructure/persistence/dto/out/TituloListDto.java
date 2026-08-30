package com.polaris.odisea.infrastructure.persistence.dto.out;

import com.polaris.odisea.domain.model.TipoContenido;

/**
 * La version ligera para el listado. Sin sinopsis: el listado de 200 titulos
 * no debe arrastrarla.
 */
public record TituloListDto(
        Long id,
        TipoContenido tipo,
        String titulo,
        Integer anio,
        String imagenUrl
) {
}
