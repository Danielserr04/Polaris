package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CuentaFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CuentaFormDtoMapper {

    CuentaFormDto toFormDto(Cuenta cuenta);
}
