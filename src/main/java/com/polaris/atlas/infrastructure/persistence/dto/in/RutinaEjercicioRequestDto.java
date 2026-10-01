package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Id de un ejercicio del catalogo o tuyo")
        @NotNull Long ejercicioId,
        @Schema(description = "Posicion en la rutina, desde 1; no se puede repetir")
        @NotNull @Min(1) @Max(999) Integer orden,
        @Schema(description = "Series previstas, de 1 a 20")
        @NotNull @Min(1) @Max(20) Integer seriesObjetivo,
        @Schema(description = "Repeticiones previstas, en texto libre (hasta 20 caracteres)", example = "8-12")
        @NotBlank @Size(max = 20) String repsObjetivo
) {
}
