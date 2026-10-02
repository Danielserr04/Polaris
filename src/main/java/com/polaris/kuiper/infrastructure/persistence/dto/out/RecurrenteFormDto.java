package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La ficha completa que devuelve el detalle.
 */
public record RecurrenteFormDto(
        Long id,
        String concepto,
        BigDecimal importe,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        String metodoPago,
        LocalDate fechaInicio,
        FrecuenciaRecurrente frecuencia,
        LocalDate proximaFecha,
        Integer cuotasTotal,
        int cuotasPagadas,
        boolean activo
) {
}
