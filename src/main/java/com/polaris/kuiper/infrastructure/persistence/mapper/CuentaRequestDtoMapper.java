package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CuentaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CuentaRequestDtoMapper {

    /** id y usuarioId los pone el servicio; saldoActual se calcula. Nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "saldoActual", ignore = true)
    Cuenta toDomain(CuentaRequestDto dto);
}
