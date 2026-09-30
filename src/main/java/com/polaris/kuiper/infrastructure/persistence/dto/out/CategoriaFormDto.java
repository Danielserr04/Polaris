package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoMovimiento;

/**
 * La ficha completa que devuelve el detalle.
 */
public record CategoriaFormDto(
        Long id,
        String nombre,
        String color,
        String icono,
        TipoMovimiento tipo
) {
}
