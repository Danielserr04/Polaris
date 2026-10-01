package com.polaris.fusion.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Un macro del dia. Sin objetivo vigente, {@code objetivo}, {@code restante} y
 * {@code porcentaje} son null.
 */
public record MacroResumenDto(
        @Schema(description = "Total del dia")
        BigDecimal consumido,
        @Schema(description = "Objetivo del dia; null si no hay objetivo vigente")
        BigDecimal objetivo,
        @Schema(description = "objetivo menos consumido; negativo si te pasaste")
        BigDecimal restante,
        @Schema(description = "consumido sobre objetivo, en %, con 2 decimales; null si no hay objetivo o es 0")
        BigDecimal porcentaje
) {
}
