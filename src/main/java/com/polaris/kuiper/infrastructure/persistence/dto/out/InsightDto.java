package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Una observacion sobre el mes, ya redactada para mostrar.
 */
public record InsightDto(
        @Schema(description = "GASTO_VS_MEDIA, CATEGORIA_SUBE, PRESUPUESTO, MAYOR_GASTO, TASA_AHORRO, "
                + "RECURRENTES_PROXIMOS o DIA_SEMANA")
        String tipo,
        @Schema(description = "AVISO, BIEN o INFO")
        String severidad,
        String titulo,
        String texto
) {
}
