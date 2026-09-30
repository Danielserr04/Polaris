package com.polaris.atlas.infrastructure.persistence.dto.out;

import java.time.LocalDate;

/**
 * La version ligera para el listado: sin series, con cuantas lleva. rutinaId y
 * rutinaNombre son nulos en un entreno libre.
 */
public record SesionListDto(
        Long id,
        LocalDate fecha,
        Long rutinaId,
        String rutinaNombre,
        Integer duracionMin,
        int numeroSeries
) {
}
