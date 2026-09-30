package com.polaris.fusion.infrastructure.persistence.dto.in;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Una linea dentro de ComidaRequestDto. cantidadG en gramos: mayor que 0 y
 * hasta 10000, con los digitos de la columna DECIMAL(7,2).
 */
public record ComidaLineaRequestDto(
        @NotNull Long alimentoId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("10000")
        @Digits(integer = 5, fraction = 2) BigDecimal cantidadG
) {
}
