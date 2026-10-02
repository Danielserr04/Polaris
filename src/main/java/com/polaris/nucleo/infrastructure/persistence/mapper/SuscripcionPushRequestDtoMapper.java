package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.SuscripcionPush;
import com.polaris.nucleo.infrastructure.persistence.dto.in.SuscripcionPushRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SuscripcionPushRequestDtoMapper {

    /** id, usuarioId y creadaEn los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    SuscripcionPush toDomain(SuscripcionPushRequestDto dto);
}
