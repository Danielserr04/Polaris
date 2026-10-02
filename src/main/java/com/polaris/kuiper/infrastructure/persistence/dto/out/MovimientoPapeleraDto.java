package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una fila de la papelera: lo mismo que MovimientoListDto mas cuando se borro.
 * El job lo elimina de verdad 30 dias despues de {@code borradoEn}.
 */
public record MovimientoPapeleraDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        String concepto,
        LocalDateTime borradoEn
) {
}
