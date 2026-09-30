package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.PresupuestoFilter;
import com.polaris.kuiper.infrastructure.persistence.dto.in.PresupuestoFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PresupuestoFilterMapper {

    PresupuestoFilter toFilter(PresupuestoFilterListDto dto);
}
