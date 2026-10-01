package com.polaris.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Formato unico de respuesta de error de toda la API.
 * Ver docs/convenciones.md, seccion "Errores".
 */
@Schema(description = "Cuerpo de toda respuesta de error de la API")
public record ErrorResponse(
        @Schema(description = "Momento del error, sin zona horaria", example = "2026-09-30T10:15:00")
        LocalDateTime timestamp,
        @Schema(description = "Codigo HTTP de la respuesta", example = "404")
        int status,
        @Schema(description = "Mensaje legible del error (en los 400 de validacion, campo: motivo)",
                example = "Entrada no encontrada: 42")
        String error,
        @Schema(description = "Ruta de la peticion", example = "/api/odisea/entrada/42")
        String path
) {

    public static ErrorResponse of(int status, String error, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, path);
    }
}
