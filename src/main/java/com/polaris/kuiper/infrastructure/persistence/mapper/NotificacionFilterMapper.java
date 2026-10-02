package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.NotificacionFilter;
import com.polaris.kuiper.infrastructure.persistence.dto.in.NotificacionFilterListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificacionFilterMapper {

    NotificacionFilter toFilter(NotificacionFilterListDto dto);
}
