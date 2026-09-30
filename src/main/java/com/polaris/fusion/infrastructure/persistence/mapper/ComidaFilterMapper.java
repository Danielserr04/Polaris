package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ComidaFilter;
import com.polaris.fusion.infrastructure.persistence.dto.in.ComidaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComidaFilterMapper {

    ComidaFilter toFilter(ComidaFilterListDto dto);
}
