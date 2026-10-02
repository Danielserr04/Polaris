package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MovimientoRequestDtoMapper {

    /** id, usuarioId y las fichas de categoria y cuenta los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cuenta", ignore = true)
    Movimiento toDomain(MovimientoRequestDto dto);
}
