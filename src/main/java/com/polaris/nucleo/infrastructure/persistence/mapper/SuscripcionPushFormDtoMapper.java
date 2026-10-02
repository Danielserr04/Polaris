package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.SuscripcionPush;
import com.polaris.nucleo.infrastructure.persistence.dto.out.SuscripcionPushFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SuscripcionPushFormDtoMapper {

    SuscripcionPushFormDto toFormDto(SuscripcionPush suscripcion);
}
