package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.util.List;

/**
 * El resumen del mes. {@code periodo} viaja como texto {@code 2026-09}.
 */
public record ResumenMensualDto(
        String periodo,
        BigDecimal ingresos,
        BigDecimal gastos,
        BigDecimal balance,
        List<GastoCategoriaDto> gastoPorCategoria
) {
}
