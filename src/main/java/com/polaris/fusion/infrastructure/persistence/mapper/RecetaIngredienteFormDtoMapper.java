package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.RecetaIngrediente;
import com.polaris.fusion.infrastructure.persistence.dto.out.RecetaIngredienteFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecetaIngredienteFormDtoMapper {

    @Mapping(target = "alimentoNombre", source = "alimento.nombre")
    @Mapping(target = "alimentoMarca", source = "alimento.marca")
    @Mapping(target = "kcal", source = "macros.kcal")
    @Mapping(target = "proteinas", source = "macros.proteinas")
    @Mapping(target = "carbohidratos", source = "macros.carbohidratos")
    @Mapping(target = "grasas", source = "macros.grasas")
    RecetaIngredienteFormDto toFormDto(RecetaIngrediente ingrediente);

    List<RecetaIngredienteFormDto> toFormDtoList(List<RecetaIngrediente> ingredientes);
}
