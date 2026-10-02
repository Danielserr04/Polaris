package com.polaris.fusion.infrastructure.persistence.dto.out;

import com.polaris.fusion.domain.model.DiaSemana;
import com.polaris.fusion.domain.model.MomentoComida;

import java.math.BigDecimal;

/**
 * Una linea del detalle: alimento con gramos o receta con raciones (los campos
 * del otro tipo se omiten). kcal y macros son los de esa cantidad.
 */
public record PlanComidaLineaFormDto(
        Long id,
        DiaSemana diaSemana,
        MomentoComida momento,
        Long alimentoId,
        String alimentoNombre,
        String alimentoMarca,
        BigDecimal cantidadG,
        Long recetaId,
        String recetaNombre,
        BigDecimal raciones,
        BigDecimal kcal,
        BigDecimal proteinas,
        BigDecimal carbohidratos,
        BigDecimal grasas
) {
}
