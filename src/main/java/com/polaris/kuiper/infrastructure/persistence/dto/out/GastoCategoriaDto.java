package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.EstadoPresupuesto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Lo gastado en una categoria contra su presupuesto mensual. limiteMensual y
 * restante son nulos si no hay presupuesto; restante es negativo si se excedio.
 */
public record GastoCategoriaDto(
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        @Schema(description = "Gasto de la categoria en el mes; 0.00 si solo tiene presupuesto")
        BigDecimal gastado,
        @Schema(description = "Presupuesto MENSUAL de la categoria; null si no tiene")
        BigDecimal limiteMensual,
        @Schema(description = "limiteMensual menos gastado; negativo si se excedio; null si no hay limite")
        BigDecimal restante,
        @Schema(description = "gastado / limiteMensual * 100 con un decimal; null si no hay limite", example = "82.5")
        BigDecimal porcentaje,
        @Schema(description = "Umbral de alerta del presupuesto (1 a 100); null si no hay limite", example = "80")
        Integer porcentajeAlerta,
        @Schema(description = "SIN_PRESUPUESTO, OK, AVISO (llega al umbral) o EXCEDIDO (gastado mayor que el limite)")
        EstadoPresupuesto estado
) {
}
