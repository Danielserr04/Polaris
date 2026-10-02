package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.infrastructure.persistence.dto.out.RecetaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecetaListDtoMapper {

    @Mapping(target = "numIngredientes", expression = "java(receta.getIngredientes().size())")
    @Mapping(target = "kcalRacion", source = "porRacion.kcal")
    @Mapping(target = "proteinasRacion", source = "porRacion.proteinas")
    @Mapping(target = "carbohidratosRacion", source = "porRacion.carbohidratos")
    @Mapping(target = "grasasRacion", source = "porRacion.grasas")
    RecetaListDto toListDto(Receta receta);

    List<RecetaListDto> toListDtoList(List<Receta> recetas);
}
