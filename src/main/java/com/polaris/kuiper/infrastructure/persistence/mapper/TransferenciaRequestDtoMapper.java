package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.infrastructure.persistence.dto.in.TransferenciaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferenciaRequestDtoMapper {

    /** id, usuarioId y las fichas de las cuentas los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "cuentaOrigen", ignore = true)
    @Mapping(target = "cuentaDestino", ignore = true)
    Transferencia toDomain(TransferenciaRequestDto dto);
}
