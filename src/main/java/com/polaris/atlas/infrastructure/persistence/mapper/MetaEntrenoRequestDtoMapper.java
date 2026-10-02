package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.infrastructure.persistence.dto.in.MetaEntrenoRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaEntrenoRequestDtoMapper {

    /** id, usuario, punto de partida y fecha de creacion los pone el servicio; lo calculado, al leer. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "valorInicial", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    @Mapping(target = "ejercicioNombre", ignore = true)
    @Mapping(target = "valorActual", ignore = true)
    @Mapping(target = "progresoPct", ignore = true)
    @Mapping(target = "conseguida", ignore = true)
    MetaEntreno toDomain(MetaEntrenoRequestDto dto);
}
