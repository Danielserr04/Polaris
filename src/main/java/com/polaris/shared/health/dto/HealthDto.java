package com.polaris.shared.health.dto;

/**
 * Lo que devuelve /health. El Controller solo habla DTOs, tambien aqui.
 */
public record HealthDto(
        String status,
        String app,
        String database
) {
}
