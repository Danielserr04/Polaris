package com.polaris.fusion.infrastructure.persistence.dto.out;

import com.polaris.fusion.domain.model.MomentoComida;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado: sin lineas, con los totales de macros.
 */
public record ComidaListDto(
        Long id,
        LocalDate fecha,
        MomentoComida momento,
        BigDecimal kcalTotal,
        BigDecimal proteinasTotal,
        BigDecimal carbohidratosTotal,
        BigDecimal grasasTotal
) {
}
