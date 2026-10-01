package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Lo que llega por query params al buscar en el catalogo externo de alimentos. */
public record CatalogoAlimentoBuscarDto(
        @NotBlank(message = "no puede estar vacio")
        @Size(max = 100, message = "no puede superar los 100 caracteres")
        @Parameter(description = "Texto a buscar, hasta 100 caracteres", example = "yogur natural")
        String q
) {
}
