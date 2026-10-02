package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.ComercioFrecuente;
import com.polaris.kuiper.infrastructure.persistence.dto.out.ComercioFrecuenteDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComercioFrecuenteDtoMapper {

    ComercioFrecuenteDto toDto(ComercioFrecuente comercio);

    List<ComercioFrecuenteDto> toDtoList(List<ComercioFrecuente> comercios);
}
