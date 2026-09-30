package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.PerfilFilter;
import com.polaris.nucleo.infrastructure.persistence.dto.in.PerfilFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Sin uso hoy: Perfil no tiene listado. Existe porque la plantilla lo exige.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PerfilFilterMapper {

    PerfilFilter toFilter(PerfilFilterListDto dto);
}
