package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.YearMonth;

/**
 * Por query param: {@code ?meses=6&hasta=2026-09}. Los dos opcionales.
 */
public record EvolucionFilterListDto(
        @Parameter(description = "Cuantos meses, de 1 a 24; por defecto 6", example = "6")
        Integer meses,
        @Parameter(description = "Ultimo mes de la serie, formato yyyy-MM; por defecto el mes actual",
                example = "2026-09")
        YearMonth hasta
) {
}
