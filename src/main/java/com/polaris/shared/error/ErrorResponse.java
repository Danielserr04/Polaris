package com.polaris.shared.error;

import java.time.LocalDateTime;

/**
 * Formato unico de respuesta de error de toda la API.
 * Ver docs/convenciones.md, seccion "Errores".
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String path
) {

    public static ErrorResponse of(int status, String error, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, path);
    }
}
