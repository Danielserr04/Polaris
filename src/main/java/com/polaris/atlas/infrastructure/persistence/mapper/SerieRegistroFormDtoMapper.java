package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.SerieRegistro;
import com.polaris.atlas.infrastructure.persistence.dto.out.SerieRegistroFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SerieRegistroFormDtoMapper {

    @Mapping(target = "ejercicioNombre", source = "ejercicio.nombre")
    @Mapping(target = "ejercicioGrupoMuscular", source = "ejercicio.grupoMuscular")
    SerieRegistroFormDto toFormDto(SerieRegistro serie);

    List<SerieRegistroFormDto> toFormDtoList(List<SerieRegistro> series);
}
