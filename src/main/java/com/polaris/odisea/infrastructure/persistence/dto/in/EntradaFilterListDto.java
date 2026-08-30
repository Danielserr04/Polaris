package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.TipoContenido;

/**
 * Los filtros que llegan por query params. Entregable de docs/roadmap.md:
 * "filtros por tipo y estado con Specifications".
 */
public record EntradaFilterListDto(
        TipoContenido tipo,
        EstadoEntrada estado
) {
}
