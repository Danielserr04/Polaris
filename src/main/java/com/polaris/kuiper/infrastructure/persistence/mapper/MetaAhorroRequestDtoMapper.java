package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MetaAhorroRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaAhorroRequestDtoMapper {

    /** id, usuarioId y creadaEn los pone el servicio; el resto se calcula. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    @Mapping(target = "importeActual", ignore = true)
    @Mapping(target = "diasRestantes", ignore = true)
    @Mapping(target = "ahorroMensualNecesario", ignore = true)
    MetaAhorro toDomain(MetaAhorroRequestDto dto);
}
