package com.polaris.atlas.infrastructure.persistence.dto.in;

/**
 * Los filtros que llegan por query params: {@code ?activa=true}. Opcional: sin
 * el, salen todas.
 */
public record RutinaFilterListDto(
        Boolean activa
) {
}
