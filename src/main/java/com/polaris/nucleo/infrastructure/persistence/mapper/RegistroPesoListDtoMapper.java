package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RegistroPesoListDtoMapper {

    RegistroPesoListDto toListDto(RegistroPeso registro);

    List<RegistroPesoListDto> toListDtoList(List<RegistroPeso> registros);
}
