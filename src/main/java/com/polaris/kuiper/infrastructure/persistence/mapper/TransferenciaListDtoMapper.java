package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.infrastructure.persistence.dto.out.TransferenciaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferenciaListDtoMapper {

    @Mapping(target = "cuentaOrigenNombre", source = "cuentaOrigen.nombre")
    @Mapping(target = "cuentaDestinoNombre", source = "cuentaDestino.nombre")
    TransferenciaListDto toListDto(Transferencia transferencia);

    List<TransferenciaListDto> toListDtoList(List<Transferencia> transferencias);
}
