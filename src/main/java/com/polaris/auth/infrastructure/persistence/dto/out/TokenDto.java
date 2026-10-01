package com.polaris.auth.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * El JWT de sesion. Lo devuelve POST /api/auth/login en el cuerpo; el login de
 * Google lo entrega al frontend en el fragmento de la URL de redireccion
 * (ver ADR 031).
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
