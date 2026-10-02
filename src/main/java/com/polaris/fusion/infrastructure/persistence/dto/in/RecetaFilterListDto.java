package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params: {@code ?q=} busca en el nombre.
 */
public record RecetaFilterListDto(
        @Parameter(description = "Texto a buscar en el nombre", example = "pollo")
        String q
) {
}
