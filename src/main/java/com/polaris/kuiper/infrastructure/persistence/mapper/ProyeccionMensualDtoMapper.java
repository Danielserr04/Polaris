package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.ProyeccionMensual;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ProyeccionMensualDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.YearMonth;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProyeccionMensualDtoMapper {

    ProyeccionMensualDto toDto(ProyeccionMensual proyeccion);

    default String map(YearMonth periodo) {
        return periodo == null ? null : periodo.toString();
    }
}
