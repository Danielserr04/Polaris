package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.infrastructure.persistence.dto.out.RecetaFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = RecetaIngredienteFormDtoMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecetaFormDtoMapper {

    @Mapping(target = "kcalTotal", source = "totales.kcal")
    @Mapping(target = "proteinasTotal", source = "totales.proteinas")
    @Mapping(target = "carbohidratosTotal", source = "totales.carbohidratos")
    @Mapping(target = "grasasTotal", source = "totales.grasas")
    @Mapping(target = "kcalRacion", source = "porRacion.kcal")
    @Mapping(target = "proteinasRacion", source = "porRacion.proteinas")
    @Mapping(target = "carbohidratosRacion", source = "porRacion.carbohidratos")
    @Mapping(target = "grasasRacion", source = "porRacion.grasas")
    RecetaFormDto toFormDto(Receta receta);
}
