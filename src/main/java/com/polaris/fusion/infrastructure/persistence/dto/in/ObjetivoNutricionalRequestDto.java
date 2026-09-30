package com.polaris.fusion.infrastructure.persistence.dto.in;

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
        @NotNull @Min(500) @Max(10000) Integer kcalDiarias,
        @NotNull @Min(0) @Max(1000) Integer proteinasObj,
        @NotNull @Min(0) @Max(1000) Integer carbosObj,
        @NotNull @Min(0) @Max(1000) Integer grasasObj,
        @NotNull LocalDate vigenteDesde
) {
}
