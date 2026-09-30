package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;

/**
 * Los filtros que llegan por query params, opcionales:
 * {@code ?periodo=MENSUAL&categoriaId=3}.
 */
public record PresupuestoFilterListDto(
        PeriodoPresupuesto periodo,
        Long categoriaId
) {
}
