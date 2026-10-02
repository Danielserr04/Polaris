package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.infrastructure.persistence.dto.out.AportacionMetaDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AportacionMetaDtoMapper {

    AportacionMetaDto toDto(AportacionMeta aportacion);

    List<AportacionMetaDto> toDtoList(List<AportacionMeta> aportaciones);
}
