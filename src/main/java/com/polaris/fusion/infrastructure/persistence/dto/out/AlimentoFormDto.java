package com.polaris.fusion.infrastructure.persistence.dto.out;

import com.polaris.fusion.domain.model.FuenteAlimento;

import java.math.BigDecimal;

/**
 * La ficha completa que devuelve el detalle.
 */
public record AlimentoFormDto(
        Long id,
        String nombre,
        String marca,
        BigDecimal kcal100g,
        BigDecimal proteinas100g,
        BigDecimal carbohidratos100g,
        BigDecimal grasas100g,
        FuenteAlimento fuenteExterna,
        String idExterno
) {
}
