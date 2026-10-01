package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.YearMonth;

/**
 * El mes a resumir, por query param: {@code ?periodo=2026-09}. Opcional, por
 * defecto el mes actual.
 */
public record ResumenFilterListDto(
        @Parameter(description = "Mes a resumir, formato yyyy-MM; por defecto el mes actual", example = "2026-09")
        YearMonth periodo
) {
}
