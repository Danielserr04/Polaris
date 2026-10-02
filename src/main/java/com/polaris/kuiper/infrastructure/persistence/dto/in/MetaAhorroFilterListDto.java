package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params, todos opcionales: {@code ?completada=false}.
 */
public record MetaAhorroFilterListDto(
        @Parameter(description = "true solo las que ya llegaron al objetivo, false solo las pendientes")
        Boolean completada
) {
}
