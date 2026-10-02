package com.polaris.nucleo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Criterios de busqueda como datos, no como Specification. Rango de fechas
 * inclusivo por ambos extremos; cualquiera de los dos puede faltar.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedidaCorporalFilter {

    private LocalDate desde;
    private LocalDate hasta;
}
