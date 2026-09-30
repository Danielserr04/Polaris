package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaLinea;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaLineaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComidaRequestDtoMapper {

    /** id y usuarioId los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    Comida toDomain(ComidaRequestDto dto);

    /** id, usuarioId y alimento (la ficha) los pone el servicio / el adaptador. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "alimento", ignore = true)
    ComidaLinea toDomain(ComidaLineaRequestDto dto);
}
