package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaEjercicio;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaEjercicioRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RutinaRequestDtoMapper {

    /** id y usuarioId los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    Rutina toDomain(RutinaRequestDto dto);

    /** id, usuarioId y ejercicio (la ficha) los pone el servicio / el adaptador. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "ejercicio", ignore = true)
    RutinaEjercicio toDomain(RutinaEjercicioRequestDto dto);
}
