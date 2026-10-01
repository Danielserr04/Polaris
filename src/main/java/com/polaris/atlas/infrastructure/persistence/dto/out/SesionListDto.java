package com.polaris.atlas.infrastructure.persistence.dto.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La version ligera para el listado: sin series, con cuantas lleva, cuantos ejercicios
 * distintos y su volumen (las series ya se cargan para contarlas: no cuesta una consulta mas).
 * rutinaId y rutinaNombre son nulos en un entreno libre.
 */
public record SesionListDto(
        Long id,
        LocalDate fecha,
        Long rutinaId,
        String rutinaNombre,
        Integer duracionMin,
        int numeroSeries,
        @Schema(description = "Ejercicios distintos de la sesion")
        int numeroEjercicios,
        @Schema(description = "Suma de repeticiones por peso de todas las series, con 2 decimales; "
                + "el peso corporal (0 kg) aporta 0")
        BigDecimal volumen
) {
}
