package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.infrastructure.persistence.dto.out.TituloListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TituloListDtoMapper {

    TituloListDto toListDto(Titulo titulo);

    List<TituloListDto> toListDtoList(List<Titulo> titulos);
}
