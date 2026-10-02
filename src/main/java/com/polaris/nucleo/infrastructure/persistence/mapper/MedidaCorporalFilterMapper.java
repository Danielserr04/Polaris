package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.MedidaCorporalFilter;
import com.polaris.nucleo.infrastructure.persistence.dto.in.MedidaCorporalFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MedidaCorporalFilterMapper {

    MedidaCorporalFilter toFilter(MedidaCorporalFilterListDto dto);
}
