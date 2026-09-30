package com.polaris.atlas.infrastructure.persistence.dto.out;

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
        BigDecimal pesoMaximo,
        int repsPesoMaximo,
        LocalDate fechaPesoMaximo,
        BigDecimal volumenMaximoSesion,
        LocalDate fechaVolumenMaximo
) {
}
