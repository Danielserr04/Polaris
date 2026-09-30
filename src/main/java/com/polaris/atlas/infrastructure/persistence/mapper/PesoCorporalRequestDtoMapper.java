package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.infrastructure.persistence.dto.in.PesoCorporalRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "Atlas<CLASS_NAME>Impl",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PesoCorporalRequestDtoMapper {

    PesoCorporal toDomain(PesoCorporalRequestDto dto);
}
