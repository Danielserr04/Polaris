package com.polaris.fusion.infrastructure.persistence.dto.in;

/**
 * Los filtros que llegan por query params: {@code ?q=arroz}. Busca en nombre y
 * marca.
 */
public record AlimentoFilterListDto(
        String q
) {
}
