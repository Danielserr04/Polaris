package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.MetaEntrenoFilter;
import com.polaris.atlas.infrastructure.persistence.dto.in.MetaEntrenoFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaEntrenoFilterMapper {

    MetaEntrenoFilter toFilter(MetaEntrenoFilterListDto dto);
}
