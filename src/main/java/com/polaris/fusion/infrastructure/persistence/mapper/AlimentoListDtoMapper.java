package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Alimento;
import com.polaris.fusion.infrastructure.persistence.dto.out.AlimentoListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AlimentoListDtoMapper {

    AlimentoListDto toListDto(Alimento alimento);

    List<AlimentoListDto> toListDtoList(List<Alimento> alimentos);
}
