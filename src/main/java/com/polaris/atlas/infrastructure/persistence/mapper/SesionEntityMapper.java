package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.infrastructure.persistence.SesionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = SerieRegistroEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SesionEntityMapper {

    /** El nombre de la rutina no vive en la Entity: SesionJpaAdapter lo rellena aparte. */
    @Mapping(target = "rutinaNombre", ignore = true)
    Sesion toDomain(SesionEntity entity);

    SesionEntity toEntity(Sesion domain);

    List<Sesion> toDomainList(List<SesionEntity> entities);
}
