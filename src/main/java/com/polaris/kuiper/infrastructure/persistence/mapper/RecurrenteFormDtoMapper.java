package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.infrastructure.persistence.dto.out.RecurrenteFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecurrenteFormDtoMapper {

    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    @Mapping(target = "cuentaNombre", source = "cuenta.nombre")
    RecurrenteFormDto toFormDto(Recurrente recurrente);
}
