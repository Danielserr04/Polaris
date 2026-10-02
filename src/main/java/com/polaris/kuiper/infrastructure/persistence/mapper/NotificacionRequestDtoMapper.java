package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.infrastructure.persistence.dto.in.NotificacionRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificacionRequestDtoMapper {

    /** Solo viaja leida (sin ella, true); el resto no se edita por HTTP. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "clave", ignore = true)
    @Mapping(target = "titulo", ignore = true)
    @Mapping(target = "texto", ignore = true)
    @Mapping(target = "enlace", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    @Mapping(target = "leida", source = "leida", defaultValue = "true")
    Notificacion toDomain(NotificacionRequestDto dto);
}
