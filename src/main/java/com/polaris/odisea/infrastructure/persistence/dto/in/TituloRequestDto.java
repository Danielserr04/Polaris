package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.FuenteExterna;
import com.polaris.odisea.domain.model.TipoContenido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Lo que llega en un POST o PUT.
 */
public record TituloRequestDto(
        @NotNull TipoContenido tipo,
        @NotBlank String titulo,
        String tituloOriginal,
        @Schema(description = "Ano de estreno o publicacion", example = "2021")
        Integer anio,
        String sinopsis,
        String imagenUrl,
        @Schema(description = "Generos en texto libre, separados por coma y espacio",
                example = "Ciencia ficcion, Aventura")
        String generos,
        @Schema(description = "Minutos en una pelicula; paginas en un libro; null en juegos")
        Integer duracionMin,
        @Schema(description = "Origen de la ficha; MANUAL para las que se dan de alta a mano")
        @NotNull FuenteExterna fuenteExterna,
        @Schema(description = "Id en la fuente externa; null si es MANUAL")
        String idExterno
) {
}
