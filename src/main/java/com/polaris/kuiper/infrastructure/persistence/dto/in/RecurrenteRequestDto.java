package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId, cuotasPagadas ni proximaFecha:
 * los pone el servicio. Mismos limites de importe que MovimientoRequestDto.
 */
public record RecurrenteRequestDto(
        @Schema(description = "Nombre del cargo; es el concepto de cada movimiento", example = "Netflix")
        @NotBlank @Size(max = 200) String concepto,
        @Schema(description = "Siempre positivo; el signo lo da tipo", example = "12.99")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal importe,
        @NotNull TipoMovimiento tipo,
        @Schema(description = "Id de una categoria tuya del mismo tipo")
        @NotNull Long categoriaId,
        @Size(max = 50) String metodoPago,
        @NotNull FrecuenciaRecurrente frecuencia,
        @Schema(description = "Primer cargo (yyyy-MM-dd). Fija el dia de cobro. Si es pasada, se generan los atrasados",
                example = "2026-09-05")
        @NotNull LocalDate fechaInicio,
        @Schema(description = "Numero de plazos; vacio si no termina nunca", example = "12")
        @Min(1) @Max(600) Integer cuotasTotal,
        @Schema(description = "false para pausarlo", defaultValue = "true")
        Boolean activo
) {
}
