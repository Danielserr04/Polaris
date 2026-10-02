package com.polaris.atlas.infrastructure.persistence.dto.out;

import com.polaris.atlas.domain.model.MetricaLogro;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Un logro del catalogo con tu progreso.
 */
public record LogroDto(
        @Schema(example = "ENTRENOS_10")
        String codigo,
        @Schema(example = "10 entrenos")
        String nombre,
        @Schema(example = "Registra 10 sesiones")
        String descripcion,
        @Schema(description = "Icono del design system", example = "dumbbell")
        String icono,
        MetricaLogro metrica,
        @Schema(description = "Valor a alcanzar, en la unidad de la metrica")
        long objetivo,
        @Schema(description = "Lo que llevas; puede pasar del objetivo")
        long progreso,
        boolean conseguido
) {
}
