package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.infrastructure.persistence.dto.in.PesoCorporalRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PesoCorporalRequestDtoMapper {

    PesoCorporal toDomain(PesoCorporalRequestDto dto);
}
