package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.math.BigDecimal;

/**
 * Un macro del dia. Sin objetivo vigente, {@code objetivo}, {@code restante} y
 * {@code porcentaje} son null.
 */
public record MacroResumenDto(
        BigDecimal consumido,
        BigDecimal objetivo,
        BigDecimal restante,
        BigDecimal porcentaje
) {
}
