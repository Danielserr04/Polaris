package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.SerieRegistro;
import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.infrastructure.persistence.dto.in.SerieRegistroRequestDto;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SesionRequestDtoMapper {

    /** id, usuarioId y rutinaNombre (la ficha) los pone el servicio / el adaptador; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "rutinaNombre", ignore = true)
    Sesion toDomain(SesionRequestDto dto);

    /** id, usuarioId y ejercicio (la ficha) los pone el servicio / el adaptador. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "ejercicio", ignore = true)
    SerieRegistro toDomain(SerieRegistroRequestDto dto);
}
