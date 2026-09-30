package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Criterios de busqueda como datos, no como Specification. {@code fecha} es un
 * dia exacto; {@code desde} y {@code hasta} son un rango inclusivo. Todos
 * opcionales y combinables.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComidaFilter {

    private LocalDate fecha;
    private LocalDate desde;
    private LocalDate hasta;
    private MomentoComida momento;
}
