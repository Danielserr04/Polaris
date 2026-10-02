package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.SuscripcionPush;
import com.polaris.nucleo.infrastructure.persistence.SuscripcionPushEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SuscripcionPushEntityMapper {

    SuscripcionPush toDomain(SuscripcionPushEntity entity);

    SuscripcionPushEntity toEntity(SuscripcionPush domain);

    List<SuscripcionPush> toDomainList(List<SuscripcionPushEntity> entities);
}
