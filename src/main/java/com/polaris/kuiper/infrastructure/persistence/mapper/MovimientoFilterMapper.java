package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.MovimientoFilter;
import com.polaris.kuiper.infrastructure.persistence.dto.in.MovimientoFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MovimientoFilterMapper {

    MovimientoFilter toFilter(MovimientoFilterListDto dto);
}
