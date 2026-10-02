package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.CategoriaComparada;
import com.polaris.kuiper.domain.model.ComparativaCategorias;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaComparadaDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ComparativaCategoriasDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComparativaCategoriasDtoMapper {

    ComparativaCategoriasDto toDto(ComparativaCategorias comparativa);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    CategoriaComparadaDto toDto(CategoriaComparada categoria);
}
