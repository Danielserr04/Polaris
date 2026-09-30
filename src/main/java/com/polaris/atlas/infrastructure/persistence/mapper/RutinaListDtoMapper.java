package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.infrastructure.persistence.dto.out.RutinaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RutinaListDtoMapper {

    RutinaListDto toListDto(Rutina rutina);

    List<RutinaListDto> toListDtoList(List<Rutina> rutinas);
}
