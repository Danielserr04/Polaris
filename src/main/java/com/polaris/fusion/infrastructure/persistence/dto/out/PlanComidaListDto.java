package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.math.BigDecimal;

/**
 * La version ligera para el listado: sin lineas, con cuantas tiene y la media diaria.
 */
public record PlanComidaListDto(
        Long id,
        String nombre,
        boolean activo,
        Integer numLineas,
        Integer diasConLineas,
        BigDecimal kcalMediaDiaria,
        BigDecimal proteinasMediaDiaria,
        BigDecimal carbohidratosMediaDiaria,
        BigDecimal grasasMediaDiaria
) {
}
