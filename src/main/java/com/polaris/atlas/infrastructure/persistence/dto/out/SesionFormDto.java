package com.polaris.atlas.infrastructure.persistence.dto.out;

import java.time.LocalDate;
import java.util.List;

/**
 * La ficha completa que devuelve el detalle: la sesion con sus series en una
 * lista plana, agrupadas por ejercicio (en el orden en que se registraron) y
 * por numeroSerie dentro de cada uno. Sin usuarioId. rutinaId y rutinaNombre
 * son nulos en un entreno libre.
 */
public record SesionFormDto(
        Long id,
        Long rutinaId,
        String rutinaNombre,
        LocalDate fecha,
        Integer duracionMin,
        String notas,
        List<SerieRegistroFormDto> series
) {
}
