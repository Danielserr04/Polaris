package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Patrimonio;
import com.polaris.kuiper.infrastructure.persistence.dto.out.PatrimonioDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PatrimonioDtoMapper {

    PatrimonioDto toDto(Patrimonio patrimonio);
}
