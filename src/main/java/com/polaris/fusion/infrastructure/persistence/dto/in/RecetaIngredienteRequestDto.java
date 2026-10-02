package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Un ingrediente dentro de RecetaRequestDto. Mismos limites que una linea de
 * comida: mas de 0 y hasta 10000 g, DECIMAL(7,2).
 */
public record RecetaIngredienteRequestDto(
        @NotNull Long alimentoId,
        @Schema(description = "Gramos para la receta entera, mayores que 0 y hasta 10000", example = "500.00")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("10000")
        @Digits(integer = 5, fraction = 2) BigDecimal cantidadG
) {
}
