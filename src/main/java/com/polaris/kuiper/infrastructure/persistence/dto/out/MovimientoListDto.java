package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado. Aplanada, no anidada, como el resto de
 * DTOs: trae nombre, color e icono de la categoria porque sin eso la fila no
 * se puede pintar, pero no el metodo de pago.
 */
public record MovimientoListDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        String concepto
) {
}
