package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.infrastructure.persistence.dto.out.NotificacionFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificacionFormDtoMapper {

    NotificacionFormDto toFormDto(Notificacion notificacion);
}
