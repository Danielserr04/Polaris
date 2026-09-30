package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.PresupuestoListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PresupuestoListDtoMapper {

    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    PresupuestoListDto toListDto(Presupuesto presupuesto);

    List<PresupuestoListDto> toListDtoList(List<Presupuesto> presupuestos);
}
