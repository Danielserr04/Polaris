package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = RutinaEjercicioFormDtoMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RutinaFormDtoMapper {

    RutinaFormDto toFormDto(Rutina rutina);
}
