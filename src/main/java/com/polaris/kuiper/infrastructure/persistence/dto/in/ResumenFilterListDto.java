package com.polaris.kuiper.infrastructure.persistence.dto.in;

import java.time.YearMonth;

/**
 * El mes a resumir, por query param: {@code ?periodo=2026-09}. Opcional, por
 * defecto el mes actual.
 */
public record ResumenFilterListDto(
        YearMonth periodo
) {
}
