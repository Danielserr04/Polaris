package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.RutinaFilter;
import com.polaris.atlas.infrastructure.persistence.dto.in.RutinaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RutinaFilterMapper {

    RutinaFilter toFilter(RutinaFilterListDto dto);
}
