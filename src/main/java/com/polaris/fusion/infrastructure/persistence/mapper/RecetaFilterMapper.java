package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.RecetaFilter;
import com.polaris.fusion.infrastructure.persistence.dto.in.RecetaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecetaFilterMapper {

    RecetaFilter toFilter(RecetaFilterListDto dto);
}
