package com.polaris.atlas.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Criterios de busqueda como datos, no como Specification. {@code desde} y
 * {@code hasta} son un rango inclusivo de fechas; {@code rutinaId} filtra las
 * sesiones hechas con esa rutina. Todos opcionales y combinables.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionFilter {

    private LocalDate desde;
    private LocalDate hasta;
    private Long rutinaId;
}
