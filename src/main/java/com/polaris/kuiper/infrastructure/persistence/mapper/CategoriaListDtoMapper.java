package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Categoria;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CategoriaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoriaListDtoMapper {

    CategoriaListDto toListDto(Categoria categoria);

    List<CategoriaListDto> toListDtoList(List<Categoria> categorias);
}
