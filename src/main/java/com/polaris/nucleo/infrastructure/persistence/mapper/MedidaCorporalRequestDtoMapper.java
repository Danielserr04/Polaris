package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.infrastructure.persistence.dto.in.MedidaCorporalRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MedidaCorporalRequestDtoMapper {

    /** id y usuarioId los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    MedidaCorporal toDomain(MedidaCorporalRequestDto dto);
}
