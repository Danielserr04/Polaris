package com.polaris.nucleo.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La ficha completa que devuelve el detalle.
 */
public record RegistroPesoFormDto(
        Long id,
        LocalDate fecha,
        BigDecimal pesoKg,
        BigDecimal grasaPct,
        String notas
) {
}
