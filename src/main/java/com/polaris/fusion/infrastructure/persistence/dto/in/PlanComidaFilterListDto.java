package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params: {@code ?activo=true} para el plan activo.
 */
public record PlanComidaFilterListDto(
        @Parameter(description = "true: solo el activo; false: los demas")
        Boolean activo
) {
}
