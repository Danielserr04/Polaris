package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.infrastructure.persistence.ComidaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = ComidaLineaEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComidaEntityMapper {

    Comida toDomain(ComidaEntity entity);

    ComidaEntity toEntity(Comida domain);

    List<Comida> toDomainList(List<ComidaEntity> entities);
}
