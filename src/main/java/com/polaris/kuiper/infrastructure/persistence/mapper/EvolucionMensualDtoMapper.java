package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.EvolucionMensual;
import com.polaris.kuiper.infrastructure.persistence.dto.out.EvolucionMensualDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.YearMonth;
import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EvolucionMensualDtoMapper {

    EvolucionMensualDto toDto(EvolucionMensual evolucion);

    List<EvolucionMensualDto> toDtoList(List<EvolucionMensual> evolucion);

    default String map(YearMonth periodo) {
        return periodo == null ? null : periodo.toString();
    }
}
