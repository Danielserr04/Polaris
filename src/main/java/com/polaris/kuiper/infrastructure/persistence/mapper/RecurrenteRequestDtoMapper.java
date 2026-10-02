package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.infrastructure.persistence.dto.in.RecurrenteRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecurrenteRequestDtoMapper {

    /** id, usuarioId, categoria, cuenta, proximaFecha y cuotasPagadas los pone el servicio. Sin activo, true. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cuenta", ignore = true)
    @Mapping(target = "proximaFecha", ignore = true)
    @Mapping(target = "cuotasPagadas", ignore = true)
    @Mapping(target = "activo", source = "activo", defaultValue = "true")
    Recurrente toDomain(RecurrenteRequestDto dto);
}
