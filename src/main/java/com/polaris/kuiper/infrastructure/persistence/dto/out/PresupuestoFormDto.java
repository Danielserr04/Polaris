package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;

import java.math.BigDecimal;

/**
 * La ficha completa que devuelve el detalle.
 */
public record PresupuestoFormDto(
        Long id,
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        PeriodoPresupuesto periodo,
        BigDecimal importeLimite,
        Integer porcentajeAlerta
) {
}
