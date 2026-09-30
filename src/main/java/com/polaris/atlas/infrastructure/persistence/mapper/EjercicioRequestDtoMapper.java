package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EjercicioRequestDtoMapper {

    /** id y usuarioId los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    Ejercicio toDomain(EjercicioRequestDto dto);
}
