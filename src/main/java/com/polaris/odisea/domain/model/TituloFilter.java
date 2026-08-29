package com.polaris.odisea.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos, no como Specification: el dominio expresa
 * que quiere filtrar, la infraestructura decide como se traduce a SQL.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TituloFilter {

    private TipoContenido tipo;
    /** Coincidencia parcial contra titulo y tituloOriginal, sin distinguir mayusculas. */
    private String texto;
}
