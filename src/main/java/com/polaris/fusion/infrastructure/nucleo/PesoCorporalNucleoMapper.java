package com.polaris.fusion.infrastructure.nucleo;

import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.domain.model.PesoCorporalFilter;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Traduce entre el modelo de Fusion y el de Nucleo. Solo lo usa
 * {@link NucleoPesoCorporalAdapter}.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PesoCorporalNucleoMapper {

    PesoCorporal toPesoCorporal(RegistroPeso registro);

    List<PesoCorporal> toPesoCorporalList(List<RegistroPeso> registros);

    /** id y usuarioId los pone el servicio de Nucleo. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    RegistroPeso toRegistroPeso(PesoCorporal peso);

    RegistroPesoFilter toRegistroPesoFilter(PesoCorporalFilter filter);
}
