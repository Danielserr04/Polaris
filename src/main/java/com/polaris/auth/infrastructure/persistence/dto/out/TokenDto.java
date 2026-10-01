package com.polaris.auth.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lo que se devuelve al terminar el login de Google.
 *
 * <p>Mientras no exista el frontend, este JSON se ve en el navegador y el token
 * se pega en el boton Authorize de Swagger. Ver docs/modulos/auth.md.
 */
public record TokenDto(
        @Schema(description = "JWT propio de Polaris, para la cabecera Authorization: Bearer")
        String token,
        @Schema(description = "Siempre Bearer", example = "Bearer")
        String tipo,
        @Schema(description = "Segundos de vida del token desde que se emite")
        long expiraEnSegundos
) {
    public static TokenDto bearer(String token, long expiraEnSegundos) {
        return new TokenDto(token, "Bearer", expiraEnSegundos);
    }
}
