package com.polaris.odisea.infrastructure.persistence.dto.in;

import com.polaris.odisea.domain.model.EstadoEntrada;
import com.polaris.odisea.domain.model.TipoContenido;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params. Entregable de docs/roadmap.md:
 * "filtros por tipo y estado con Specifications".
 */
public record EntradaFilterListDto(
        @Parameter(description = "Tipo de contenido del titulo")
        TipoContenido tipo,
        @Parameter(description = "Estado de la entrada")
        EstadoEntrada estado
) {
}
