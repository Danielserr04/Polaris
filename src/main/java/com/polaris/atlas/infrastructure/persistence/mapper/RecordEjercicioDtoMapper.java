package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.RecordEjercicio;
import com.polaris.atlas.infrastructure.persistence.dto.out.RecordEjercicioDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordEjercicioDtoMapper {

    RecordEjercicioDto toDto(RecordEjercicio record);

    List<RecordEjercicioDto> toDtoList(List<RecordEjercicio> records);
}
