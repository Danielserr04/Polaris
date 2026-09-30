package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.PeriodoPresupuesto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Lo que llega en un POST o PUT. Sin usuarioId: lo pone el servicio a partir
 * del JWT, nunca del body. Los limites de digitos son los de la columna
 * DECIMAL(10,2), para que un valor que no cabe sea un 400 y no un error de MySQL.
 */
public record PresupuestoRequestDto(
        @NotNull Long categoriaId,
        @NotNull PeriodoPresupuesto periodo,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal importeLimite
) {
}
