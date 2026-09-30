package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.infrastructure.persistence.RutinaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = RutinaEjercicioEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RutinaEntityMapper {

    Rutina toDomain(RutinaEntity entity);

    RutinaEntity toEntity(Rutina domain);

    List<Rutina> toDomainList(List<RutinaEntity> entities);
}
