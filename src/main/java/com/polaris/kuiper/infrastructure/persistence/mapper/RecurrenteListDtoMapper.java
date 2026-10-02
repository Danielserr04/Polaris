package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.infrastructure.persistence.dto.out.RecurrenteListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecurrenteListDtoMapper {

    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    @Mapping(target = "cuentaNombre", source = "cuenta.nombre")
    RecurrenteListDto toListDto(Recurrente recurrente);

    List<RecurrenteListDto> toListDtoList(List<Recurrente> recurrentes);
}
