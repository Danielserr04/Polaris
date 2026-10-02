package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.infrastructure.persistence.dto.out.TrabajoMuscularDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TrabajoMuscularDtoMapper {

    TrabajoMuscularDto toDto(TrabajoMuscular trabajo);

    List<TrabajoMuscularDto> toDtoList(List<TrabajoMuscular> trabajos);
}
