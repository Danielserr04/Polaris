package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.AvisoRecordatorio;
import com.polaris.nucleo.infrastructure.persistence.dto.out.RecordatorioPendienteDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordatorioPendienteDtoMapper {

    List<RecordatorioPendienteDto> toDto(List<AvisoRecordatorio> avisos);
}
