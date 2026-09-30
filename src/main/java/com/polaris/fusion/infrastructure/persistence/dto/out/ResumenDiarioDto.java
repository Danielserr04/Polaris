package com.polaris.fusion.infrastructure.persistence.dto.out;

import java.time.LocalDate;

/**
 * El resumen del dia. {@code objetivoVigenteDesde} es null si no habia objetivo.
 */
public record ResumenDiarioDto(
        LocalDate fecha,
        LocalDate objetivoVigenteDesde,
        MacroResumenDto kcal,
        MacroResumenDto proteinas,
        MacroResumenDto carbohidratos,
        MacroResumenDto grasas
) {
}
