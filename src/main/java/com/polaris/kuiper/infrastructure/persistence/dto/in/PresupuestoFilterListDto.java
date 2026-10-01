package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params, opcionales:
 * {@code ?periodo=MENSUAL&categoriaId=3}.
 */
public record PresupuestoFilterListDto(
        @Parameter(description = "Periodo del presupuesto: MENSUAL o ANUAL")
        PeriodoPresupuesto periodo,
        @Parameter(description = "Id de la categoria")
        Long categoriaId
) {
}
