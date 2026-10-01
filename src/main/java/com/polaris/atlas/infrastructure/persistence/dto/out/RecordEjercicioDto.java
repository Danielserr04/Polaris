package com.polaris.atlas.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Las mejores marcas de un ejercicio. {@code volumenMaximoSesion} y
 * {@code fechaVolumenMaximo} se omiten (null) si el volumen mas alto es 0.
 */
public record RecordEjercicioDto(
        Long ejercicioId,
        String ejercicioNombre,
        String ejercicioGrupoMuscular,
        @Schema(description = "Mayor peso, en kg, levantado en una serie")
        BigDecimal pesoMaximo,
        @Schema(description = "Repeticiones de la serie con mas reps a ese peso")
        int repsPesoMaximo,
        @Schema(description = "Primera fecha en que se logro ese peso con esas repeticiones")
        LocalDate fechaPesoMaximo,
        @Schema(description = "Mayor volumen (repeticiones por peso) del ejercicio en una sola sesion; null si es 0")
        BigDecimal volumenMaximoSesion,
        @Schema(description = "Fecha de esa sesion; null si no hay volumen")
        LocalDate fechaVolumenMaximo
) {
}
