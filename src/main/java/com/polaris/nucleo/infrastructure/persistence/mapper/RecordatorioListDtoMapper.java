package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.Recordatorio;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RecordatorioListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordatorioListDtoMapper {

    RecordatorioListDto toListDto(Recordatorio recordatorio);

    List<RecordatorioListDto> toListDto(List<Recordatorio> recordatorios);

    /** De 1 (lunes) a 7 (domingo), en orden. */
    default List<Integer> diasANumeros(Set<DayOfWeek> dias) {
        return dias == null ? List.of() : dias.stream().sorted().map(DayOfWeek::getValue).toList();
    }
}
