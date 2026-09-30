package com.polaris.kuiper.infrastructure.persistence.dto.out;

import com.polaris.kuiper.domain.model.TipoMovimiento;

/**
 * La version ligera para el listado. Categoria ya es pequena, asi que hoy
 * coincide con el FormDto; se mantienen separados por la plantilla.
 */
public record CategoriaListDto(
        Long id,
        String nombre,
        String color,
        String icono,
        TipoMovimiento tipo
) {
}
