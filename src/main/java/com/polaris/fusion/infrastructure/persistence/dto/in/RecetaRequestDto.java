package com.polaris.fusion.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lo que llega en un POST o PUT: la receta con sus ingredientes anidados. Sin
 * usuarioId: lo pone el servicio a partir del JWT. De 1 a 50 ingredientes; el
 * PUT reemplaza el conjunto entero.
 */
public record RecetaRequestDto(
        @Schema(example = "Pollo al curry")
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @Schema(description = "Para cuantas raciones salen los ingredientes, de 1 a 50", example = "4")
        @NotNull @Min(1) @Max(50) Integer raciones,
        @Size(max = 10000) String instrucciones,
        @Schema(description = "De 1 a 50 ingredientes, en gramos para la receta entera")
        @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid RecetaIngredienteRequestDto> ingredientes
) {
}
