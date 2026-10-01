package com.polaris.shared.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lo que devuelve /health. El Controller solo habla DTOs, tambien aqui.
 */
public record HealthDto(
        @Schema(description = "Estado global: UP, o DOWN si MySQL no responde", example = "UP")
        String status,
        @Schema(description = "Nombre de la aplicacion", example = "polaris")
        String app,
        @Schema(description = "Estado de la conexion con MySQL: UP o DOWN", example = "UP")
        String database
) {
}
