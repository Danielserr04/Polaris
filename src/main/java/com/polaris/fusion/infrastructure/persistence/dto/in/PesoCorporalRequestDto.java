package com.polaris.fusion.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lo que llega en un POST. Sin usuarioId: sale del JWT. Los limites son los de
 * la columna de Nucleo (DECIMAL(5,2), DECIMAL(4,1) y TEXT) para que un valor
 * que no cabe sea un 400 y no un error de MySQL.
 */
public record PesoCorporalRequestDto(
        @NotNull @PastOrPresent LocalDate fecha,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 3, fraction = 2) BigDecimal pesoKg,
        @DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 1) BigDecimal grasaPct,
        @Size(max = 65535, message = "maximo 65535 caracteres") String notas
) {
}
