package com.polaris.nucleo.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Vacio a proposito. Existe porque la plantilla lo exige
 * (docs/plantilla-modulo.md), pero el listado son siempre los cuatro tipos
 * del usuario: no hay nada que filtrar.
 */
@Getter
@Builder
@NoArgsConstructor
public class RecordatorioFilter {
}
