package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.CalculoObjetivo;
import com.polaris.fusion.infrastructure.persistence.dto.out.CalculoObjetivoDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CalculoObjetivoDtoMapper {

    CalculoObjetivoDto toDto(CalculoObjetivo calculo);
}
