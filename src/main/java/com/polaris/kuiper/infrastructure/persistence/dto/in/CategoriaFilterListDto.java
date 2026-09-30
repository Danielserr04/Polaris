package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoMovimiento;

/**
 * Los filtros que llegan por query params.
 */
public record CategoriaFilterListDto(
        TipoMovimiento tipo
) {
}
