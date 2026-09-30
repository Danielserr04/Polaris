package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;

import java.math.BigDecimal;

/**
 * La version ligera para el listado. Presupuesto ya es pequeno, asi que hoy
 * coincide con el FormDto; se mantienen separados por la plantilla.
 */
public record PresupuestoListDto(
        Long id,
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        PeriodoPresupuesto periodo,
        BigDecimal importeLimite
) {
}
