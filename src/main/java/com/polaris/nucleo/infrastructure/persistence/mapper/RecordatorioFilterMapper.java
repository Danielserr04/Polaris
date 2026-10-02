package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.RecordatorioFilter;
import com.polaris.nucleo.infrastructure.persistence.dto.in.RecordatorioFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Sin uso hoy: el listado no tiene filtros. Existe porque la plantilla lo exige.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordatorioFilterMapper {

    RecordatorioFilter toFilter(RecordatorioFilterListDto dto);
}
