package com.polaris.shared.logro;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Un logro del catalogo con tu progreso. Lo devuelven los /logros de todos los
 * modulos, con la misma forma, para que la pantalla de logros los junte.
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
        NivelLogro nivel,
        @Schema(description = "Como se cuenta el progreso", example = "sesiones")
        String unidad,
        @Schema(description = "Valor a alcanzar, en la unidad del logro")
        long objetivo,
        @Schema(description = "Lo que llevas; puede pasar del objetivo")
        long progreso,
        boolean conseguido,
        @Schema(description = "Dia en que lo conseguiste; nulo si no lo tienes o si el dato no tiene fecha",
                example = "2026-09-14")
        LocalDate fechaConseguido
) {
}
