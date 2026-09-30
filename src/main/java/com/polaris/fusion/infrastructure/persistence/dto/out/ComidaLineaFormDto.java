package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.math.BigDecimal;

/**
 * Una linea del detalle. kcal y macros son los de esta cantidad, no por 100 g.
 */
public record ComidaLineaFormDto(
        Long id,
        Long alimentoId,
        String alimentoNombre,
        String alimentoMarca,
        BigDecimal cantidadG,
        BigDecimal kcal,
        BigDecimal proteinas,
        BigDecimal carbohidratos,
        BigDecimal grasas
) {
}
