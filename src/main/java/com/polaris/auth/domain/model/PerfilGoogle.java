package com.polaris.auth.domain.model;

/**
 * Lo que Google cuenta de ti al terminar el login, ya traducido al lenguaje
 * del dominio.
 *
 * <p>Existe para que UsuarioService no reciba un {@code OAuth2User} de Spring:
 * el dominio no sabe que el login llega por OAuth2, solo que alguien se ha
 * identificado y trae estos cuatro datos.
 */
public record PerfilGoogle(
        String googleId,
        String email,
        String nombre,
        String avatarUrl
) {
}
