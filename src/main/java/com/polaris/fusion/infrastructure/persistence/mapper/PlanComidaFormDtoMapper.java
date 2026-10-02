package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.infrastructure.persistence.dto.out.PlanComidaFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = PlanComidaLineaFormDtoMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanComidaFormDtoMapper {

    @Mapping(target = "kcalMediaDiaria", source = "mediaDiaria.kcal")
    @Mapping(target = "proteinasMediaDiaria", source = "mediaDiaria.proteinas")
    @Mapping(target = "carbohidratosMediaDiaria", source = "mediaDiaria.carbohidratos")
    @Mapping(target = "grasasMediaDiaria", source = "mediaDiaria.grasas")
    PlanComidaFormDto toFormDto(PlanComida plan);
}
