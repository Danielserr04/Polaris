package com.polaris.atlas.infrastructure.persistence.dto.in;

/**
 * Los filtros que llegan por query params: {@code ?grupoMuscular=pecho} y
 * {@code ?q=press}. {@code q} busca en el nombre.
 */
public record EjercicioFilterListDto(
        String grupoMuscular,
        String q
) {
}
