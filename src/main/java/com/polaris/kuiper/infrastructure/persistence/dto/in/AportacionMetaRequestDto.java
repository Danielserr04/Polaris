package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cuerpo de POST /api/kuiper/meta/{id}/aportacion. Que el importe no sea 0,
 * que la fecha no sea futura y que no se retire mas de lo ahorrado lo
 * comprueba el servicio.
 */
public record AportacionMetaRequestDto(
        @Schema(description = "Positivo aporta, negativo retira. Distinto de 0", example = "150.00")
        @NotNull @Digits(integer = 8, fraction = 2) BigDecimal importe,
        @Schema(description = "yyyy-MM-dd; sin ella, hoy. No puede ser futura", example = "2026-10-02")
        LocalDate fecha,
        @Schema(description = "Opcional", example = "Paga extra")
        @Size(max = 255) String nota
) {
}
