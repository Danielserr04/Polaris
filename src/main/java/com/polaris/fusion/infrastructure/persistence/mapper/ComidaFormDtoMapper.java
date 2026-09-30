package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = ComidaLineaFormDtoMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComidaFormDtoMapper {

    @Mapping(target = "kcalTotal", source = "totales.kcal")
    @Mapping(target = "proteinasTotal", source = "totales.proteinas")
    @Mapping(target = "carbohidratosTotal", source = "totales.carbohidratos")
    @Mapping(target = "grasasTotal", source = "totales.grasas")
    ComidaFormDto toFormDto(Comida comida);
}
