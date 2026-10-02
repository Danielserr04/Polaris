package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.math.BigDecimal;

/**
 * La version ligera para el listado: sin ingredientes ni instrucciones, con
 * cuantos ingredientes tiene y los macros por racion.
 */
public record RecetaListDto(
        Long id,
        String nombre,
        String descripcion,
        Integer raciones,
        Integer numIngredientes,
        BigDecimal kcalRacion,
        BigDecimal proteinasRacion,
        BigDecimal carbohidratosRacion,
        BigDecimal grasasRacion
) {
}
