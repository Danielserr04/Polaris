package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MovimientoListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MovimientoListDtoMapper {

    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    MovimientoListDto toListDto(Movimiento movimiento);

    List<MovimientoListDto> toListDtoList(List<Movimiento> movimientos);
}
