package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Criterios de busqueda como datos, no como Specification. El rango de fechas
 * es inclusivo por ambos extremos; todos los campos son opcionales.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoFilter {

    private LocalDate desde;
    private LocalDate hasta;
    private Long categoriaId;
    private TipoMovimiento tipo;
    private Long cuentaId;
}
