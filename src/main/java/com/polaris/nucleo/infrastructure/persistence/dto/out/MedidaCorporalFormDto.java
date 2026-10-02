package com.polaris.nucleo.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La ficha completa que devuelve el detalle.
 */
public record MedidaCorporalFormDto(
        Long id,
        LocalDate fecha,
        BigDecimal cuelloCm,
        BigDecimal pechoCm,
        BigDecimal cinturaCm,
        BigDecimal caderaCm,
        BigDecimal brazoIzqCm,
        BigDecimal brazoDchoCm,
        BigDecimal musloIzqCm,
        BigDecimal musloDchoCm,
        String notas
) {
}
