package com.polaris.fusion.infrastructure.nucleo;

import com.polaris.fusion.domain.model.DatosCorporales;
import com.polaris.nucleo.domain.model.Perfil;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Traduce el Perfil de Nucleo al modelo de Fusion. Los enums (sexo, nivel de
 * actividad) se traducen por nombre. Solo lo usa {@link NucleoDatosCorporalesAdapter}.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DatosCorporalesNucleoMapper {

    DatosCorporales toDatosCorporales(Perfil perfil);
}
