package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.domain.model.PlanComidaLinea;
import com.polaris.fusion.infrastructure.persistence.dto.in.PlanComidaLineaRequestDto;
import com.polaris.fusion.infrastructure.persistence.dto.in.PlanComidaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanComidaRequestDtoMapper {

    /** id, usuarioId y activo los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "activo", ignore = true)
    PlanComida toDomain(PlanComidaRequestDto dto);

    /** id, usuarioId y las fichas de alimento y receta los pone el servicio / el adaptador. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "alimento", ignore = true)
    @Mapping(target = "receta", ignore = true)
    PlanComidaLinea toDomain(PlanComidaLineaRequestDto dto);
}
