package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.infrastructure.persistence.dto.in.PresupuestoRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PresupuestoRequestDtoMapper {

    /** id, usuarioId y categoria los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    Presupuesto toDomain(PresupuestoRequestDto dto);
}
