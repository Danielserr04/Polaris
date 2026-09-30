package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.atlas.infrastructure.persistence.dto.out.ProgresionSesionDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProgresionSesionDtoMapper {

    ProgresionSesionDto toDto(ProgresionSesion sesion);

    List<ProgresionSesionDto> toDtoList(List<ProgresionSesion> sesiones);
}
