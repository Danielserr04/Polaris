package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PlanComida;
import com.polaris.fusion.infrastructure.persistence.dto.out.PlanComidaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanComidaListDtoMapper {

    @Mapping(target = "numLineas", expression = "java(plan.getLineas().size())")
    @Mapping(target = "kcalMediaDiaria", source = "mediaDiaria.kcal")
    @Mapping(target = "proteinasMediaDiaria", source = "mediaDiaria.proteinas")
    @Mapping(target = "carbohidratosMediaDiaria", source = "mediaDiaria.carbohidratos")
    @Mapping(target = "grasasMediaDiaria", source = "mediaDiaria.grasas")
    PlanComidaListDto toListDto(PlanComida plan);

    List<PlanComidaListDto> toListDtoList(List<PlanComida> planes);
}
