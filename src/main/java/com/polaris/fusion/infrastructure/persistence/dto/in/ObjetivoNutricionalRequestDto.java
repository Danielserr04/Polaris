package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Lo que llega en un POST. Sin id ni usuarioId: los pone el servicio a partir
 * del JWT, nunca del body. vigenteDesde es obligatoria y puede ser futura
 * (un objetivo "desde el lunes"). No se exige que las kcal cuadren con los macros.
 */
public record ObjetivoNutricionalRequestDto(
        @Schema(description = "kcal objetivo al dia, de 500 a 10000", example = "2400")
        @NotNull @Min(500) @Max(10000) Integer kcalDiarias,
        @Schema(description = "Gramos de proteina al dia, de 0 a 1000")
        @NotNull @Min(0) @Max(1000) Integer proteinasObj,
        @Schema(description = "Gramos de carbohidratos al dia, de 0 a 1000")
        @NotNull @Min(0) @Max(1000) Integer carbosObj,
        @Schema(description = "Gramos de grasa al dia, de 0 a 1000")
        @NotNull @Min(0) @Max(1000) Integer grasasObj,
        @Schema(description = "Primer dia en que rige (yyyy-MM-dd); puede ser futuro", example = "2026-10-01")
        @NotNull LocalDate vigenteDesde
) {
}
