package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La ficha completa que devuelve el detalle.
 */
public record TransferenciaFormDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        Long cuentaOrigenId,
        String cuentaOrigenNombre,
        String cuentaOrigenColor,
        Long cuentaDestinoId,
        String cuentaDestinoNombre,
        String cuentaDestinoColor,
        String concepto
) {
}
