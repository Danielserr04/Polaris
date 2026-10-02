package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.domain.model.RecetaIngrediente;
import com.polaris.fusion.infrastructure.persistence.dto.in.RecetaIngredienteRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.RecetaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecetaRequestDtoMapper {

    /** id y usuarioId los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    Receta toDomain(RecetaRequestDto dto);

    /** id, usuarioId y alimento (la ficha) los pone el servicio / el adaptador. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "alimento", ignore = true)
    RecetaIngrediente toDomain(RecetaIngredienteRequestDto dto);
}
