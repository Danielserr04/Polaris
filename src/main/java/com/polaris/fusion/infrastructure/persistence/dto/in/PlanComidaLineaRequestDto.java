package com.polaris.fusion.infrastructure.persistence.dto.in;

import com.polaris.fusion.domain.model.DiaSemana;
import com.polaris.fusion.domain.model.MomentoComida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Una linea dentro de PlanComidaRequestDto: alimentoId con cantidadG, o
 * recetaId con raciones (el servicio da 400 si llegan los dos o ninguno).
 */
public record PlanComidaLineaRequestDto(
        @NotNull DiaSemana diaSemana,
        @NotNull MomentoComida momento,
        Long alimentoId,
        @Schema(description = "Gramos del alimento, mayores que 0 y hasta 10000", example = "150.00")
        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("10000")
        @Digits(integer = 5, fraction = 2) BigDecimal cantidadG,
        Long recetaId,
        @Schema(description = "Raciones de la receta, mayores que 0 y hasta 50", example = "1.5")
        @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("50")
        @Digits(integer = 3, fraction = 2) BigDecimal raciones
) {
}
