package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.TransferenciaFilter;
import com.polaris.kuiper.infrastructure.persistence.dto.in.TransferenciaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferenciaFilterMapper {

    TransferenciaFilter toFilter(TransferenciaFilterListDto dto);
}
