package com.polaris.atlas.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Lo trabajado de un grupo muscular en el rango.
 */
public record TrabajoMuscularDto(
        @Schema(description = "Grupo muscular tal como esta escrito en el ejercicio", example = "Pecho")
        String grupoMuscular,
        @Schema(description = "Series de ejercicios de ese grupo")
        int numeroSeries,
        @Schema(description = "Sesiones distintas en las que se trabajo")
        int numeroSesiones,
        @Schema(description = "Suma de repeticiones por peso, con 2 decimales; 0 con peso corporal")
        BigDecimal volumen
) {
}
