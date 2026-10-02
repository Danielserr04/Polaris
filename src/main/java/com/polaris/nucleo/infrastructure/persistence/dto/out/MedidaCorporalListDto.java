package com.polaris.nucleo.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado: sin notas, que pueden ser largas.
 */
public record MedidaCorporalListDto(
        Long id,
        LocalDate fecha,
        BigDecimal cuelloCm,
        BigDecimal pechoCm,
        BigDecimal cinturaCm,
        BigDecimal caderaCm,
        BigDecimal brazoIzqCm,
        BigDecimal brazoDchoCm,
        BigDecimal musloIzqCm,
        BigDecimal musloDchoCm
) {
}
