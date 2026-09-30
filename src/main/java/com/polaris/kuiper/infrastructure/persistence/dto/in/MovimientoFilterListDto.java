package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoMovimiento;

import java.time.LocalDate;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?desde=2026-09-01&hasta=2026-09-30&categoriaId=3&tipo=GASTO}.
 */
public record MovimientoFilterListDto(
        LocalDate desde,
        LocalDate hasta,
        Long categoriaId,
        TipoMovimiento tipo
) {
}
