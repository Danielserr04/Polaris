package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.infrastructure.persistence.dto.out.MetaEntrenoListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaEntrenoListDtoMapper {

    MetaEntrenoListDto toListDto(MetaEntreno meta);

    List<MetaEntrenoListDto> toListDtoList(List<MetaEntreno> metas);
}
