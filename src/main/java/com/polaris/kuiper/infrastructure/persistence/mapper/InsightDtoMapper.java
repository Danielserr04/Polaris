package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Insight;
import com.polaris.kuiper.infrastructure.persistence.dto.out.InsightDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InsightDtoMapper {

    InsightDto toDto(Insight insight);

    List<InsightDto> toDtoList(List<Insight> insights);
}
