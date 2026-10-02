package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.FrecuenciaRecurrente;
import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado: sin metodo de pago ni fecha de inicio.
 */
public record RecurrenteListDto(
        Long id,
        String concepto,
        BigDecimal importe,
        TipoMovimiento tipo,
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        FrecuenciaRecurrente frecuencia,
        LocalDate proximaFecha,
        Integer cuotasTotal,
        int cuotasPagadas,
        boolean activo,
        Long cuentaId,
        String cuentaNombre
) {
}
