package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.CuentaFilter;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CuentaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CuentaFilterMapper {

    CuentaFilter toFilter(CuentaFilterListDto dto);
}
