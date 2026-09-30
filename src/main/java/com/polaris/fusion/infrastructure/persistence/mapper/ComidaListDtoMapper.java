package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.infrastructure.persistence.dto.out.ComidaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComidaListDtoMapper {

    @Mapping(target = "kcalTotal", source = "totales.kcal")
    @Mapping(target = "proteinasTotal", source = "totales.proteinas")
    @Mapping(target = "carbohidratosTotal", source = "totales.carbohidratos")
    @Mapping(target = "grasasTotal", source = "totales.grasas")
    ComidaListDto toListDto(Comida comida);

    List<ComidaListDto> toListDtoList(List<Comida> comidas);
}
