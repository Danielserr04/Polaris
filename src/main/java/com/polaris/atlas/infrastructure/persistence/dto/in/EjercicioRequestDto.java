package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId ni esPropio: un ejercicio creado
 * por la API es siempre del usuario del JWT, nunca del catalogo. Los limites de
 * tamano son los de la columna.
 */
public record EjercicioRequestDto(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 50) String grupoMuscular,
        @Schema(description = "Material que usa; vacio si es con el peso corporal", example = "barra")
        @Size(max = 100) String equipamiento
) {
}
