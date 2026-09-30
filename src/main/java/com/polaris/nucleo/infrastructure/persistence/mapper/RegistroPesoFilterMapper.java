package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.RegistroPesoFilter;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RegistroPesoFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RegistroPesoFilterMapper {

    RegistroPesoFilter toFilter(RegistroPesoFilterListDto dto);
}
