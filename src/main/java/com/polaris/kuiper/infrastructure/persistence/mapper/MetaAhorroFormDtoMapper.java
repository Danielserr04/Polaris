package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.infrastructure.persistence.dto.out.MetaAhorroFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** porcentaje, restante y completada salen de los getters calculados de MetaAhorro. */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaAhorroFormDtoMapper {

    MetaAhorroFormDto toFormDto(MetaAhorro meta);
}
