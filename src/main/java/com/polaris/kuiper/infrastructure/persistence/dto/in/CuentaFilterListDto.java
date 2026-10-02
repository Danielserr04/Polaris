package com.polaris.kuiper.infrastructure.persistence.dto.in;

import com.polaris.kuiper.domain.model.TipoCuenta;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params, todos opcionales:
 * {@code ?archivada=false&tipo=AHORRO}.
 */
public record CuentaFilterListDto(
        @Parameter(description = "true solo las archivadas, false solo las activas")
        Boolean archivada,
        @Parameter(description = "CORRIENTE, AHORRO, TARJETA o EFECTIVO")
        TipoCuenta tipo
) {
}
