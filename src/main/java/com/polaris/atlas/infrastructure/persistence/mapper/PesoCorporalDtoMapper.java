package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.infrastructure.persistence.dto.out.PesoCorporalDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "Atlas<CLASS_NAME>Impl",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PesoCorporalDtoMapper {

    PesoCorporalDto toDto(PesoCorporal peso);

    List<PesoCorporalDto> toDtoList(List<PesoCorporal> pesos);
}
