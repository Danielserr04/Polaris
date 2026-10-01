package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoMovimiento;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params.
 */
public record CategoriaFilterListDto(
        @Parameter(description = "Tipo de categoria: INGRESO o GASTO")
        TipoMovimiento tipo
) {
}
