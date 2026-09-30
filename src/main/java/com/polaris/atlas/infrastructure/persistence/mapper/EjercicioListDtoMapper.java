package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.infrastructure.persistence.dto.out.EjercicioListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EjercicioListDtoMapper {

    /** esPropio sale de usuarioId (Ejercicio.isPropio); el usuarioId no llega al DTO. */
    @Mapping(target = "esPropio", source = "propio")
    EjercicioListDto toListDto(Ejercicio ejercicio);

    List<EjercicioListDto> toListDtoList(List<Ejercicio> ejercicios);
}
