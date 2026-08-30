package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.infrastructure.persistence.dto.out.ResultadoCatalogoDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ResultadoCatalogoDtoMapper {

    ResultadoCatalogoDto toDto(ResultadoCatalogo resultado);

    List<ResultadoCatalogoDto> toDtoList(List<ResultadoCatalogo> resultados);
}
