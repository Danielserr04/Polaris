package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lo que llega en un POST o PUT: el plan con sus lineas anidadas. Sin
 * usuarioId ni activo: el usuario sale del JWT y se activa con su propio
 * endpoint. De 0 a 200 lineas; el PUT reemplaza el conjunto entero.
 */
public record PlanComidaRequestDto(
        @Schema(example = "Semana de definicion")
        @NotBlank @Size(max = 120) String nombre,
        @Schema(description = "De 0 a 200 lineas; en el PUT sustituyen a las anteriores")
        @NotNull @Size(max = 200) List<@NotNull @Valid PlanComidaLineaRequestDto> lineas
) {
}
