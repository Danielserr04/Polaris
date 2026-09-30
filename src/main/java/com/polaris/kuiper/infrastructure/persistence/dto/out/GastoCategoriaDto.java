package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;

/**
 * Lo gastado en una categoria contra su presupuesto mensual. limiteMensual y
 * restante son nulos si no hay presupuesto; restante es negativo si se excedio.
 */
public record GastoCategoriaDto(
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        BigDecimal gastado,
        BigDecimal limiteMensual,
        BigDecimal restante
) {
}
