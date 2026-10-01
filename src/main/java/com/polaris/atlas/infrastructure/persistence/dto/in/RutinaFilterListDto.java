package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params: {@code ?activa=true}. Opcional: sin
 * el, salen todas.
 */
public record RutinaFilterListDto(
        @Parameter(description = "true solo las activas, false solo las inactivas; sin valor, todas")
        Boolean activa
) {
}
