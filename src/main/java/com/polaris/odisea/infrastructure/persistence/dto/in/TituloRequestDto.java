package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Lo que llega en un POST o PUT.
 */
public record TituloRequestDto(
        @NotNull TipoContenido tipo,
        @NotBlank String titulo,
        String tituloOriginal,
        Integer anio,
        String sinopsis,
        String imagenUrl,
        String generos,
        Integer duracionMin,
        @NotNull FuenteExterna fuenteExterna,
        String idExterno
) {
}
