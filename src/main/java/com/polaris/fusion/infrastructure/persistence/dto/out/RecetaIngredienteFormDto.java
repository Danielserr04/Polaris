package com.polaris.fusion.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Un ingrediente del detalle. kcal y macros son los de esta cantidad, no por 100 g.
 */
public record RecetaIngredienteFormDto(
        Long id,
        Long alimentoId,
        String alimentoNombre,
        String alimentoMarca,
        @Schema(description = "Gramos para la receta entera")
        BigDecimal cantidadG,
        @Schema(description = "kcal de esta cantidad, no por 100 g")
        BigDecimal kcal,
        BigDecimal proteinas,
        BigDecimal carbohidratos,
        BigDecimal grasas
) {
}
