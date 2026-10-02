package com.polaris.kuiper.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una linea del historial de una meta. Sin usuarioId ni metaId: van en la ruta.
 */
public record AportacionMetaDto(
        Long id,
        LocalDate fecha,
        BigDecimal importe,
        String nota
) {
}
