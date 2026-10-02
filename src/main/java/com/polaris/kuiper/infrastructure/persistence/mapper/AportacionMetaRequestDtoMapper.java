package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.infrastructure.persistence.dto.in.AportacionMetaRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AportacionMetaRequestDtoMapper {

    /** id, usuarioId y metaId los pone el servicio (metaId viene de la ruta). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "metaId", ignore = true)
    AportacionMeta toDomain(AportacionMetaRequestDto dto);
}
