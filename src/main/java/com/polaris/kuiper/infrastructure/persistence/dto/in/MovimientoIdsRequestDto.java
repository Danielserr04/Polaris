package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Cuerpo de POST /movimiento/borrar. El tope evita un IN de miles de valores
 * desde un cliente roto; la seleccion de la pantalla es de un mes.
 */
public record MovimientoIdsRequestDto(
        @Schema(description = "Ids de movimientos tuyos, entre 1 y 500", example = "[12, 15, 18]")
        @NotEmpty @Size(max = 500) List<@NotNull Long> ids
) {
}
