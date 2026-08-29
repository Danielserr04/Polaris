package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.TituloFilter;
import com.polaris.odisea.infrastructure.persistence.dto.in.TituloFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TituloFilterMapper {

    TituloFilter toFilter(TituloFilterListDto dto);
}
