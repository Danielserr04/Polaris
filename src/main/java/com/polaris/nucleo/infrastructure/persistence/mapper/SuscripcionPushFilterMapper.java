package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.SuscripcionPushFilter;
import com.polaris.nucleo.infrastructure.persistence.dto.in.SuscripcionPushFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Sin uso hoy: las suscripciones no se listan. Existe porque la plantilla lo exige.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SuscripcionPushFilterMapper {

    SuscripcionPushFilter toFilter(SuscripcionPushFilterListDto dto);
}
