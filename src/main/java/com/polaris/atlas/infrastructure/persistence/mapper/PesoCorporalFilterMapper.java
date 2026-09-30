package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.PesoCorporalFilter;
import com.polaris.atlas.infrastructure.persistence.dto.in.PesoCorporalFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "Atlas<CLASS_NAME>Impl",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PesoCorporalFilterMapper {

    PesoCorporalFilter toFilter(PesoCorporalFilterListDto dto);
}
