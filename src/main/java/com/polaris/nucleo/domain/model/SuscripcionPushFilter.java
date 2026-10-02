package com.polaris.nucleo.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Vacio a proposito. Existe porque la plantilla lo exige
 * (docs/plantilla-modulo.md); las suscripciones no se listan por HTTP.
 */
@Getter
@Builder
@NoArgsConstructor
public class SuscripcionPushFilter {
}
