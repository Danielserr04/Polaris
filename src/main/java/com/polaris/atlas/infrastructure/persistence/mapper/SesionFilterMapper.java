package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.SesionFilter;
import com.polaris.atlas.infrastructure.persistence.dto.in.SesionFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SesionFilterMapper {

    SesionFilter toFilter(SesionFilterListDto dto);
}
