package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AlimentoFormDtoMapper {

    AlimentoFormDto toFormDto(Alimento alimento);
}
