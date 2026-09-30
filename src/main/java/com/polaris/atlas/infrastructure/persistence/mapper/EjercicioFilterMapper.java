package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.EjercicioFilter;
import com.polaris.atlas.infrastructure.persistence.dto.in.EjercicioFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EjercicioFilterMapper {

    @Mapping(target = "texto", source = "q")
    EjercicioFilter toFilter(EjercicioFilterListDto dto);
}
