package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.infrastructure.persistence.dto.in.ProgresionFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProgresionFilterMapper {

    ProgresionFilter toFilter(ProgresionFilterListDto dto);
}
