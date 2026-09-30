package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.GastoCategoria;
import com.polaris.kuiper.domain.model.ResumenMensual;
import com.polaris.kuiper.infrastructure.persistence.dto.out.GastoCategoriaDto;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ResumenMensualDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.YearMonth;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ResumenMensualDtoMapper {

    ResumenMensualDto toDto(ResumenMensual resumen);

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "categoriaColor", source = "categoria.color")
    @Mapping(target = "categoriaIcono", source = "categoria.icono")
    GastoCategoriaDto toDto(GastoCategoria gasto);

    default String map(YearMonth periodo) {
        return periodo == null ? null : periodo.toString();
    }
}
