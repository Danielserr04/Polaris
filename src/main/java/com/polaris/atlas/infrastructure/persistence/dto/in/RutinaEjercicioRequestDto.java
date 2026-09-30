package com.polaris.atlas.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Una linea dentro de RutinaRequestDto. orden desde 1 (que no se repita lo
 * valida el servicio); seriesObjetivo de 1 a 20; repsObjetivo es texto libre
 * ("8-12", "5", "AMRAP") de hasta 20 caracteres, como la columna.
 */
public record RutinaEjercicioRequestDto(
        @NotNull Long ejercicioId,
        @NotNull @Min(1) @Max(999) Integer orden,
        @NotNull @Min(1) @Max(20) Integer seriesObjetivo,
        @NotBlank @Size(max = 20) String repsObjetivo
) {
}
