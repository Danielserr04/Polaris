package com.polaris.auth.infrastructure.persistence.dto.out;

import java.time.Instant;

/**
 * La ficha del usuario autenticado. El Controller solo habla DTOs.
 */
public record UsuarioFormDto(
        Long id,
        String email,
        String nombre,
        String avatarUrl,
        Instant creadoEn
) {
}
