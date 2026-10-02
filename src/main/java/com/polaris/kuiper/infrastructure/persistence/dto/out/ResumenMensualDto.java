package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * El resumen del mes. {@code periodo} viaja como texto {@code 2026-09}.
 */
public record ResumenMensualDto(
        @Schema(description = "Mes resumido, formato yyyy-MM", example = "2026-09")
        String periodo,
        @Schema(description = "Suma de los ingresos del mes")
        BigDecimal ingresos,
        @Schema(description = "Suma de los gastos del mes")
        BigDecimal gastos,
        @Schema(description = "ingresos menos gastos; negativo si se gasto mas de lo ingresado")
        BigDecimal balance,
        List<GastoCategoriaDto> gastoPorCategoria,
        @Schema(description = "Suma de los presupuestos MENSUALES; 0.00 si no hay ninguno")
        BigDecimal presupuestoTotal,
        @Schema(description = "Cuantas categorias estan en AVISO")
        int categoriasEnAviso,
        @Schema(description = "Cuantas categorias estan EXCEDIDAS")
        int categoriasExcedidas
) {
}
