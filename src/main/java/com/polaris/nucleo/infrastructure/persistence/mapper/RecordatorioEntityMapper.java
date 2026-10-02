package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.infrastructure.persistence.RecordatorioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordatorioEntityMapper {

    Recordatorio toDomain(RecordatorioEntity entity);

    RecordatorioEntity toEntity(Recordatorio domain);

    List<Recordatorio> toDomainList(List<RecordatorioEntity> entities);

    /** "1,3,5" a lunes, miercoles y viernes. */
    default Set<DayOfWeek> diasDeTexto(String dias) {
        Set<DayOfWeek> resultado = EnumSet.noneOf(DayOfWeek.class);
        if (dias == null || dias.isBlank()) {
            return resultado;
        }
        Arrays.stream(dias.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .map(DayOfWeek::of)
                .forEach(resultado::add);
        return resultado;
    }

    /** Lunes, miercoles y viernes a "1,3,5", siempre en orden. */
    default String diasATexto(Set<DayOfWeek> dias) {
        if (dias == null) {
            return "";
        }
        return dias.stream()
                .sorted()
                .map(d -> String.valueOf(d.getValue()))
                .collect(Collectors.joining(","));
    }
}
