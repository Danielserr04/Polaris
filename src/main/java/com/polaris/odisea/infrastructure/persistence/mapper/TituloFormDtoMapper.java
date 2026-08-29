package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TituloFormDtoMapper {

    TituloFormDto toFormDto(Titulo titulo);
}
