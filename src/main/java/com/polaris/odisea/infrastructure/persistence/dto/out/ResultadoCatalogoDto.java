package com.polaris.odisea.infrastructure.persistence.dto.out;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;

/**
 * Un resultado de la busqueda externa, todavia sin guardar.
 *
 * <p>tituloId viene relleno si esa ficha ya esta en el catalogo de Polaris, y
 * a null si no. Es lo que permite al frontend ensenar "anadir" o "ya lo
 * tienes" sin una segunda llamada.
 */
public record ResultadoCatalogoDto(
        FuenteExterna fuenteExterna,
        String idExterno,
        TipoContenido tipo,
        String titulo,
        String tituloOriginal,
        Integer anio,
        String sinopsis,
        String imagenUrl,
        Long tituloId
) {
}
