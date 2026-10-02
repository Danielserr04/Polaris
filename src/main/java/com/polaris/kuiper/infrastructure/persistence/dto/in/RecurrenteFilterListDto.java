package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoMovimiento;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?activo=true&tipo=GASTO&categoriaId=3}.
 */
public record RecurrenteFilterListDto(
        @Parameter(description = "true solo los activos, false solo los pausados o terminados")
        Boolean activo,
        @Parameter(description = "INGRESO o GASTO")
        TipoMovimiento tipo,
        @Parameter(description = "Id de la categoria")
        Long categoriaId
) {
}
