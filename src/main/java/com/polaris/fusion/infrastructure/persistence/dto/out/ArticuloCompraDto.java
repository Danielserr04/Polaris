package com.polaris.fusion.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Una fila de la lista de la compra.
 */
public record ArticuloCompraDto(
        Long alimentoId,
        String nombre,
        String marca,
        @Schema(description = "Gramos para toda la semana")
        BigDecimal cantidadG
) {
}
