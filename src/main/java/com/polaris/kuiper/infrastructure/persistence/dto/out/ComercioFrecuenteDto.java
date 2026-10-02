package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Un concepto de gasto agrupado sin distinguir espacios, mayusculas ni tildes.
 */
public record ComercioFrecuenteDto(
        @Schema(description = "La forma escrita mas repetida del concepto", example = "Mercadona")
        String nombre,
        BigDecimal total,
        @Schema(description = "Cuantos gastos con ese concepto")
        int veces,
        @Schema(description = "total entre veces")
        BigDecimal ticketMedio
) {
}
