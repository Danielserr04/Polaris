package com.polaris.nucleo.infrastructure.persistence.dto.out;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado: sin notas, que pueden ser largas.
 */
public record RegistroPesoListDto(
        Long id,
        LocalDate fecha,
        BigDecimal pesoKg,
        BigDecimal grasaPct
) {
}
