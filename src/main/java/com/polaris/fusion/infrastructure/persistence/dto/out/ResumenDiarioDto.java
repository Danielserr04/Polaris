package com.polaris.fusion.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * El resumen del dia. {@code objetivoVigenteDesde} es null si no habia objetivo.
 */
public record ResumenDiarioDto(
        LocalDate fecha,
        @Schema(description = "Fecha de inicio del objetivo aplicado; null si no habia ninguno")
        LocalDate objetivoVigenteDesde,
        MacroResumenDto kcal,
        MacroResumenDto proteinas,
        MacroResumenDto carbohidratos,
        MacroResumenDto grasas
) {
}
