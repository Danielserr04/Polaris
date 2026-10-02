package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.TrabajoMuscularFilter;
import com.polaris.atlas.infrastructure.persistence.dto.in.TrabajoMuscularFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TrabajoMuscularFilterMapper {

    TrabajoMuscularFilter toFilter(TrabajoMuscularFilterListDto dto);
}
