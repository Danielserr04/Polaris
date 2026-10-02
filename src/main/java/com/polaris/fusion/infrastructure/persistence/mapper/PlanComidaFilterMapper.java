package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PlanComidaFilter;
import com.polaris.fusion.infrastructure.persistence.dto.in.PlanComidaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanComidaFilterMapper {

    PlanComidaFilter toFilter(PlanComidaFilterListDto dto);
}
