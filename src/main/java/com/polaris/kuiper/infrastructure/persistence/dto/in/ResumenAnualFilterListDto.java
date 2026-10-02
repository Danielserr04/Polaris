package com.polaris.kuiper.infrastructure.persistence.dto.in;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * El anio a resumir, por query param: {@code ?anio=2026}. Opcional, por
 * defecto el anio actual.
 */
public record ResumenAnualFilterListDto(
        @Parameter(description = "Anio a resumir; por defecto el actual", example = "2026")
        @Min(1900) @Max(9999) Integer anio
) {
}
