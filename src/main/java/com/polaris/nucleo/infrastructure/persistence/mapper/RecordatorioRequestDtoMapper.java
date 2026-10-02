package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RecordatorioRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordatorioRequestDtoMapper {

    /** id, usuarioId y las marcas de dia las pone el servicio; el tipo, el controller desde la ruta. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "tipo", ignore = true)
    @Mapping(target = "avisadoEn", ignore = true)
    @Mapping(target = "descartadoEn", ignore = true)
    Recordatorio toDomain(RecordatorioRequestDto dto);

    /** 1 (lunes) a 7 (domingo); los repetidos cuentan una vez. */
    default Set<DayOfWeek> diasDeNumeros(List<Integer> dias) {
        Set<DayOfWeek> resultado = EnumSet.noneOf(DayOfWeek.class);
        if (dias != null) {
            dias.forEach(d -> resultado.add(DayOfWeek.of(d)));
        }
        return resultado;
    }
}
