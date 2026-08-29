package com.polaris.odisea.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos, no como Specification. {@code tipo}
 * filtra por el titulo referenciado, no por un campo propio de Entrada.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaFilter {

    private TipoContenido tipo;
    private EstadoEntrada estado;
}
