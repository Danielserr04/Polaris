package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.infrastructure.persistence.dto.out.MetaEntrenoFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaEntrenoFormDtoMapper {

    MetaEntrenoFormDto toFormDto(MetaEntreno meta);
}
