package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.MacroResumen;
import com.polaris.fusion.domain.model.ResumenDiario;
import com.polaris.fusion.infrastructure.persistence.dto.out.MacroResumenDto;
import com.polaris.fusion.infrastructure.persistence.dto.out.ResumenDiarioDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ResumenDiarioDtoMapper {

    ResumenDiarioDto toDto(ResumenDiario resumen);

    MacroResumenDto toDto(MacroResumen macro);
}
