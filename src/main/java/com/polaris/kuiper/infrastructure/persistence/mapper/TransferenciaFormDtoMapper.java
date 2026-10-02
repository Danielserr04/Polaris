package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.infrastructure.persistence.dto.out.TransferenciaFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferenciaFormDtoMapper {

    @Mapping(target = "cuentaOrigenNombre", source = "cuentaOrigen.nombre")
    @Mapping(target = "cuentaOrigenColor", source = "cuentaOrigen.color")
    @Mapping(target = "cuentaDestinoNombre", source = "cuentaDestino.nombre")
    @Mapping(target = "cuentaDestinoColor", source = "cuentaDestino.color")
    TransferenciaFormDto toFormDto(Transferencia transferencia);
}
