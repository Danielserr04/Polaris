package com.polaris.fusion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda como datos. {@code activo} filtra el plan activo (true)
 * o los demas (false); null, todos.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanComidaFilter {

    private Boolean activo;
}
