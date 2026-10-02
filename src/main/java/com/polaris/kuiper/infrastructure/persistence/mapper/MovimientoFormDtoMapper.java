package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MovimientoFormDtoMapper {

    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    @Mapping(target = "cuentaNombre", source = "cuenta.nombre")
    MovimientoFormDto toFormDto(Movimiento movimiento);
}
