package com.polaris.atlas.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lo que llega en un POST o PUT: la rutina con sus lineas anidadas. Sin
 * usuarioId: lo pone el servicio a partir del JWT, nunca del body. De 1 a 50
 * lineas; el PUT reemplaza el conjunto entero. Los limites de tamano son los
 * de la columna (descripcion es TEXT: se limita a 2000 por sensatez).
 */
public record RutinaRequestDto(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 2000) String descripcion,
        @Schema(description = "false retira la rutina sin borrarla")
        @NotNull Boolean activa,
        @Schema(description = "De 1 a 50 ejercicios; en el PUT sustituyen a las anteriores")
        @NotNull @Size(min = 1, max = 50) List<@NotNull @Valid RutinaEjercicioRequestDto> lineas
) {
}
