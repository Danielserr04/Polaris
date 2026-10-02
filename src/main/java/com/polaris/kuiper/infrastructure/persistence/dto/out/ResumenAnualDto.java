package com.polaris.kuiper.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Los presupuestos ANUALES del usuario frente a lo gastado en el anio.
 */
public record ResumenAnualDto(
        @Schema(description = "Anio resumido", example = "2026")
        int anio,
        @Schema(description = "Una fila por presupuesto anual, del mas consumido al menos; vacia si no hay")
        List<GastoAnualCategoriaDto> presupuestos
) {
}
