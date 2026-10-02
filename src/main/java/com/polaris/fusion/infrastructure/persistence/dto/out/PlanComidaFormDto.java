package com.polaris.fusion.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * La ficha completa del plan: lineas ordenadas por dia y momento, cada una con
 * sus macros, y la media diaria de los dias que tienen algo.
 */
public record PlanComidaFormDto(
        Long id,
        String nombre,
        boolean activo,
        List<PlanComidaLineaFormDto> lineas,
        @Schema(description = "Dias de la semana con alguna linea")
        Integer diasConLineas,
        BigDecimal kcalMediaDiaria,
        BigDecimal proteinasMediaDiaria,
        BigDecimal carbohidratosMediaDiaria,
        BigDecimal grasasMediaDiaria
) {
}
