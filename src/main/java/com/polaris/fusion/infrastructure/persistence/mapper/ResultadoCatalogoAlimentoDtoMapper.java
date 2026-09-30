package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ResultadoCatalogoAlimento;
import com.polaris.fusion.infrastructure.persistence.dto.out.ResultadoCatalogoAlimentoDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ResultadoCatalogoAlimentoDtoMapper {

    ResultadoCatalogoAlimentoDto toDto(ResultadoCatalogoAlimento resultado);

    List<ResultadoCatalogoAlimentoDto> toDtoList(List<ResultadoCatalogoAlimento> resultados);
}
