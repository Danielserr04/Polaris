package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.CategoriaFilter;
import com.polaris.kuiper.infrastructure.persistence.dto.in.CategoriaFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoriaFilterMapper {

    CategoriaFilter toFilter(CategoriaFilterListDto dto);
}
