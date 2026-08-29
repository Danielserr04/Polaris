package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.EntradaFilter;
import com.polaris.odisea.infrastructure.persistence.dto.in.EntradaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EntradaFilterMapper {

    EntradaFilter toFilter(EntradaFilterListDto dto);
}
