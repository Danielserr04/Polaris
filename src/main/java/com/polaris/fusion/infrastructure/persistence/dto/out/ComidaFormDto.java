package com.polaris.fusion.infrastructure.persistence.dto.out;

import com.polaris.fusion.domain.model.MomentoComida;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * La ficha completa que devuelve el detalle: lineas con sus macros y los
 * totales de la comida. Todo calculado al vuelo, nada guardado.
 */
public record ComidaFormDto(
        Long id,
        LocalDate fecha,
        MomentoComida momento,
        List<ComidaLineaFormDto> lineas,
        BigDecimal kcalTotal,
        BigDecimal proteinasTotal,
        BigDecimal carbohidratosTotal,
        BigDecimal grasasTotal
) {
}
