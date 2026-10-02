package com.polaris.nucleo.infrastructure.persistence.dto.out;

import java.time.LocalDateTime;

/**
 * Lo que devuelve el alta. Sin las claves: no hacen falta fuera del backend.
 */
public record SuscripcionPushFormDto(
        Long id,
        String endpoint,
        LocalDateTime creadaEn
) {
}
