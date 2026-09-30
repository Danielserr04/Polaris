package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PesoCorporal;
import com.polaris.fusion.infrastructure.persistence.dto.out.PesoCorporalDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PesoCorporalDtoMapper {

    PesoCorporalDto toDto(PesoCorporal peso);

    List<PesoCorporalDto> toDtoList(List<PesoCorporal> pesos);
}
