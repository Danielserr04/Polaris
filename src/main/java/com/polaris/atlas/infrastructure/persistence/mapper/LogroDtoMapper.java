package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Logro;
import com.polaris.atlas.infrastructure.persistence.dto.out.LogroDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LogroDtoMapper {

    LogroDto toDto(Logro logro);

    List<LogroDto> toDtoList(List<Logro> logros);
}
