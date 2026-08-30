package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EntradaRequestDtoMapper {

    /** id, usuarioId y titulo los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "titulo", ignore = true)
    Entrada toDomain(EntradaRequestDto dto);
}
