package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.RecurrenteFilter;
import com.polaris.kuiper.infrastructure.persistence.dto.in.RecurrenteFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecurrenteFilterMapper {

    RecurrenteFilter toFilter(RecurrenteFilterListDto dto);
}
