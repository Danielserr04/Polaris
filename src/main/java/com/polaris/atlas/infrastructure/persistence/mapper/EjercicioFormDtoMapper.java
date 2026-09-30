package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EjercicioFormDtoMapper {

    /** esPropio sale de usuarioId (Ejercicio.isPropio); el usuarioId no llega al DTO. */
    @Mapping(target = "esPropio", source = "propio")
    EjercicioFormDto toFormDto(Ejercicio ejercicio);
}
