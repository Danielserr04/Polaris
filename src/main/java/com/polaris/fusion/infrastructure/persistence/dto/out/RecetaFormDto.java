package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * La ficha completa: ingredientes con sus macros, totales de la receta entera
 * y por racion. Todo calculado al vuelo, nada guardado.
 */
public record RecetaFormDto(
        Long id,
        String nombre,
        String descripcion,
        Integer raciones,
        String instrucciones,
        List<RecetaIngredienteFormDto> ingredientes,
        BigDecimal kcalTotal,
        BigDecimal proteinasTotal,
        BigDecimal carbohidratosTotal,
        BigDecimal grasasTotal,
        BigDecimal kcalRacion,
        BigDecimal proteinasRacion,
        BigDecimal carbohidratosRacion,
        BigDecimal grasasRacion
) {
}
