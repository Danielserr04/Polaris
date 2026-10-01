package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params: {@code ?q=arroz}. Busca en nombre y
 * marca.
 */
public record AlimentoFilterListDto(
        @Parameter(description = "Texto que debe contener el nombre o la marca, sin distinguir mayusculas",
                example = "arroz")
        String q
) {
}
