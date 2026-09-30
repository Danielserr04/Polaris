package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RegistroPesoFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RegistroPesoFormDtoMapper {

    RegistroPesoFormDto toFormDto(RegistroPeso registro);
}
