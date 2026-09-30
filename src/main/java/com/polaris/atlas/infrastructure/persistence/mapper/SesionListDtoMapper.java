package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.infrastructure.persistence.dto.out.SesionListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SesionListDtoMapper {

    SesionListDto toListDto(Sesion sesion);

    List<SesionListDto> toListDtoList(List<Sesion> sesiones);
}
