package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.AlimentoFilter;
import com.polaris.fusion.infrastructure.persistence.dto.in.AlimentoFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AlimentoFilterMapper {

    /** El query param es ?q= (docs/modulos/fusion.md); en el dominio se llama texto. */
    @Mapping(target = "texto", source = "q")
    AlimentoFilter toFilter(AlimentoFilterListDto dto);
}
