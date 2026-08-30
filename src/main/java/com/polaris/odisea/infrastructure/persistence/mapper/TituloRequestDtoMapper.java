package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TituloRequestDtoMapper {

    /** El id lo pone el servicio, nunca llega en el body de la peticion. */
    @Mapping(target = "id", ignore = true)
    Titulo toDomain(TituloRequestDto dto);
}
