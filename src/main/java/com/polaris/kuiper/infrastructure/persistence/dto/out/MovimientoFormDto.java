package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La ficha completa que devuelve el detalle.
 */
public record MovimientoFormDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        String concepto,
        String metodoPago,
        boolean recurrente,
        Long cuentaId,
        String cuentaNombre
) {
}
