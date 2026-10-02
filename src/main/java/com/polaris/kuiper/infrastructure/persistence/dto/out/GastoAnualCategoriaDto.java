package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.EstadoPresupuesto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Lo gastado en el anio en una categoria contra su presupuesto ANUAL.
 */
public record GastoAnualCategoriaDto(
        Long categoriaId,
        String categoriaNombre,
        String categoriaColor,
        String categoriaIcono,
        @Schema(description = "Gasto de la categoria en el anio; 0.00 si no ha gastado nada")
        BigDecimal gastado,
        @Schema(description = "Presupuesto ANUAL de la categoria")
        BigDecimal limite,
        @Schema(description = "limite menos gastado; negativo si se excedio")
        BigDecimal restante,
        @Schema(description = "gastado / limite * 100 con un decimal", example = "45.3")
        BigDecimal porcentaje,
        @Schema(description = "Umbral de alerta del presupuesto (1 a 100)", example = "80")
        Integer porcentajeAlerta,
        @Schema(description = "OK, AVISO (llega al umbral) o EXCEDIDO (gastado mayor que el limite)")
        EstadoPresupuesto estado
) {
}
