package com.polaris.shared.logro;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LogroDtoMapper {

    LogroDto toDto(Logro logro);

    List<LogroDto> toDtoList(List<Logro> logros);
}
