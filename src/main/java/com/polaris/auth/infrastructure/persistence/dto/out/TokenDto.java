package com.polaris.auth.infrastructure.persistence.dto.out;

/**
 * Lo que se devuelve al terminar el login de Google.
 *
 * <p>Mientras no exista el frontend, este JSON se ve en el navegador y el token
 * se pega en el boton Authorize de Swagger. Ver docs/modulos/auth.md.
 */
public record TokenDto(
        String token,
        String tipo,
        long expiraEnSegundos
) {
    public static TokenDto bearer(String token, long expiraEnSegundos) {
        return new TokenDto(token, "Bearer", expiraEnSegundos);
    }
}
