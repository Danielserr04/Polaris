package com.polaris.kuiper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos, no como Specification. Todos opcionales.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionFilter {

    /** true: solo las no leidas. Nulo o false: todas. */
    private Boolean soloNoLeidas;
}
