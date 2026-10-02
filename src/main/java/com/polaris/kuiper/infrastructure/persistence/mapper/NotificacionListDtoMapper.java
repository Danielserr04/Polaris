package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.infrastructure.persistence.dto.out.NotificacionListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificacionListDtoMapper {

    NotificacionListDto toListDto(Notificacion notificacion);

    List<NotificacionListDto> toListDtoList(List<Notificacion> notificaciones);
}
