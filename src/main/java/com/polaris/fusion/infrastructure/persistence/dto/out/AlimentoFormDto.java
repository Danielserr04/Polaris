package com.polaris.fusion.infrastructure.persistence.dto.out;

import com.polaris.fusion.domain.model.FuenteAlimento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * La ficha completa que devuelve el detalle.
 */
public record AlimentoFormDto(
        Long id,
        String nombre,
        String marca,
        @Schema(description = "kcal por cada 100 g")
        BigDecimal kcal100g,
        BigDecimal proteinas100g,
        BigDecimal carbohidratos100g,
        BigDecimal grasas100g,
        @Schema(description = "Origen de la ficha: MANUAL u OPEN_FOOD_FACTS")
        FuenteAlimento fuenteExterna,
        @Schema(description = "Codigo de barras en Open Food Facts; null si es MANUAL")
        String idExterno
) {
}
