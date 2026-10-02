package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.MedidaCorporal;
import com.polaris.nucleo.infrastructure.persistence.dto.out.MedidaCorporalFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MedidaCorporalFormDtoMapper {

    MedidaCorporalFormDto toFormDto(MedidaCorporal registro);
}
