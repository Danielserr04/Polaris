package com.polaris.atlas.infrastructure.nucleo;

import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.domain.model.PesoCorporalFilter;
import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Traduce entre el modelo de Atlas y el de Nucleo. Solo lo usa
 * {@link NucleoPesoCorporalAdapter}.
 */
@Mapper(componentModel = "spring", implementationName = "Atlas<CLASS_NAME>Impl",
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
