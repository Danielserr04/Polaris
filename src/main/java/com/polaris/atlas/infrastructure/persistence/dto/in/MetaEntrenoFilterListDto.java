package com.polaris.atlas.infrastructure.persistence.dto.in;

import com.polaris.atlas.domain.model.TipoMetaEntreno;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Los filtros que llegan por query params: {@code ?tipo=PESO_CORPORAL}.
 */
public record MetaEntrenoFilterListDto(
        @Parameter(description = "Solo las metas de este tipo")
        TipoMetaEntreno tipo
) {
}
