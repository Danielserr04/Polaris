package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId: lo pone el servicio a partir
 * del JWT. Mismos limites de importe y fecha que MovimientoRequestDto.
 */
public record TransferenciaRequestDto(
        @Schema(description = "Id de la cuenta de la que sale el dinero; tuya")
        @NotNull Long cuentaOrigenId,
        @Schema(description = "Id de la cuenta a la que llega; tuya y distinta del origen")
        @NotNull Long cuentaDestinoId,
        @Schema(description = "Siempre positivo, hasta 8 enteros y 2 decimales", example = "200.00")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal importe,
        @Schema(description = "Dia de la transferencia (yyyy-MM-dd); no puede ser futuro", example = "2026-10-01")
        @NotNull @PastOrPresent LocalDate fecha,
        @Size(max = 255) String concepto
) {
}
