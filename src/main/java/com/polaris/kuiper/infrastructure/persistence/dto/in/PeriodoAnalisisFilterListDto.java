package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.YearMonth;

/**
 * El mes a analizar, por query param: {@code ?periodo=2026-09}. Opcional, por
 * defecto el mes actual.
 */
public record PeriodoAnalisisFilterListDto(
        @Parameter(description = "Mes a analizar, formato yyyy-MM; por defecto el mes actual", example = "2026-09")
        YearMonth periodo
) {
}
