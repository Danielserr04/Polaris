package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.RutinaEjercicio;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaEjercicioFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RutinaEjercicioFormDtoMapper {

    @Mapping(target = "ejercicioNombre", source = "ejercicio.nombre")
    @Mapping(target = "ejercicioGrupoMuscular", source = "ejercicio.grupoMuscular")
    RutinaEjercicioFormDto toFormDto(RutinaEjercicio linea);

    List<RutinaEjercicioFormDto> toFormDtoList(List<RutinaEjercicio> lineas);
}
