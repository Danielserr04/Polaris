package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

/**
 * Cuerpo opcional de POST /movimiento/{id}/duplicar. Sin cuerpo, o sin fecha,
 * la copia es de hoy.
 */
public record MovimientoDuplicarRequestDto(
        @Schema(description = "Dia de la copia (yyyy-MM-dd); hoy si no viene. No puede ser futuro",
                example = "2026-09-30")
        @PastOrPresent LocalDate fecha
) {
}
