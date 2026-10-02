package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params, todos opcionales: {@code ?soloNoLeidas=true}.
 */
public record NotificacionFilterListDto(
        @Parameter(description = "true para traer solo las no leidas")
        Boolean soloNoLeidas
) {
}
