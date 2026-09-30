package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Criterios de busqueda como datos. Rango de fechas inclusivo por ambos
 * extremos; cualquiera de los dos puede faltar.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PesoCorporalFilter {

    private LocalDate desde;
    private LocalDate hasta;
}
