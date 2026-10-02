package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.infrastructure.persistence.NotificacionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificacionEntityMapper {

    Notificacion toDomain(NotificacionEntity entity);

    NotificacionEntity toEntity(Notificacion domain);

    List<Notificacion> toDomainList(List<NotificacionEntity> entities);
}
