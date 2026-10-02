package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado: los nombres de las cuentas, sin colores.
 */
public record TransferenciaListDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        Long cuentaOrigenId,
        String cuentaOrigenNombre,
        Long cuentaDestinoId,
        String cuentaDestinoNombre,
        String concepto
) {
}
