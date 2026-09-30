package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.infrastructure.persistence.dto.in.ObjetivoNutricionalRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ObjetivoNutricionalRequestDtoMapper {

    /** id y usuarioId los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    ObjetivoNutricional toDomain(ObjetivoNutricionalRequestDto dto);
}
