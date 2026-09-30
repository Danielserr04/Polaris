package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.infrastructure.persistence.dto.out.PerfilListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Sin uso hoy: Perfil no tiene listado. Existe porque la plantilla lo exige.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PerfilListDtoMapper {

    PerfilListDto toListDto(Perfil perfil);
}
