package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = SerieRegistroFormDtoMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SesionFormDtoMapper {

    SesionFormDto toFormDto(Sesion sesion);
}
