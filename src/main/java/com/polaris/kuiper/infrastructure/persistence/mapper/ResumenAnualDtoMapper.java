package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.GastoAnualCategoria;
import com.polaris.kuiper.domain.model.ResumenAnual;
import com.polaris.kuiper.infrastructure.persistence.dto.out.GastoAnualCategoriaDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ResumenAnualDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ResumenAnualDtoMapper {

    ResumenAnualDto toDto(ResumenAnual resumen);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    GastoAnualCategoriaDto toDto(GastoAnualCategoria gasto);
}
