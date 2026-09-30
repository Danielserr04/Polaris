package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ObjetivoNutricional;
import com.polaris.fusion.infrastructure.persistence.dto.out.ObjetivoNutricionalFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ObjetivoNutricionalFormDtoMapper {

    ObjetivoNutricionalFormDto toFormDto(ObjetivoNutricional objetivo);

    List<ObjetivoNutricionalFormDto> toFormDtoList(List<ObjetivoNutricional> objetivos);
}
