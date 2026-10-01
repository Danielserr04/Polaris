package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params: {@code ?grupoMuscular=pecho} y
 * {@code ?q=press}. {@code q} busca en el nombre.
 */
public record EjercicioFilterListDto(
        @Parameter(description = "Grupo muscular, igualdad exacta sin distinguir mayusculas ni tildes",
                example = "pecho")
        String grupoMuscular,
        @Parameter(description = "Texto que debe contener el nombre, sin distinguir mayusculas", example = "press")
        String q
) {
}
