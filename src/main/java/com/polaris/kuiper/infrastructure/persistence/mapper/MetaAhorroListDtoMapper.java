package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MetaAhorroListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaAhorroListDtoMapper {

    MetaAhorroListDto toListDto(MetaAhorro meta);

    List<MetaAhorroListDto> toListDtoList(List<MetaAhorro> metas);
}
