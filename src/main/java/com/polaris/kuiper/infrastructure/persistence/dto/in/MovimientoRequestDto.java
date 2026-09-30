package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId: lo pone el servicio a partir
 * del JWT, nunca del body. El importe va siempre positivo (el signo lo pone
 * tipo) y los limites de digitos son los de la columna DECIMAL(10,2), para que
 * un valor que no cabe sea un 400 y no un error de MySQL.
 */
public record MovimientoRequestDto(
        @NotNull @PastOrPresent LocalDate fecha,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal importe,
        @NotNull TipoMovimiento tipo,
        @NotNull Long categoriaId,
        @Size(max = 255) String concepto,
        @Size(max = 50) String metodoPago,
        boolean recurrente
) {
}
