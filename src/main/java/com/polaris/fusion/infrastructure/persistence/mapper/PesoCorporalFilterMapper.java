package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PesoCorporalFilter;
import com.polaris.fusion.infrastructure.persistence.dto.in.PesoCorporalFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PesoCorporalFilterMapper {

    PesoCorporalFilter toFilter(PesoCorporalFilterListDto dto);
}
