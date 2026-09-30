package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AlimentoRequestDtoMapper {

    /** id, fuenteExterna e idExterno los pone el servicio; nunca llegan en el body. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fuenteExterna", ignore = true)
    @Mapping(target = "idExterno", ignore = true)
    Alimento toDomain(AlimentoRequestDto dto);
}
