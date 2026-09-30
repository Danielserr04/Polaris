package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.math.BigDecimal;

/**
 * La version ligera para el listado: sin fuente ni idExterno, que solo
 * importan al importar.
 */
public record AlimentoListDto(
        Long id,
        String nombre,
        String marca,
        BigDecimal kcal100g,
        BigDecimal proteinas100g,
        BigDecimal carbohidratos100g,
        BigDecimal grasas100g
) {
}
