package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.infrastructure.persistence.dto.out.CuentaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CuentaListDtoMapper {

    CuentaListDto toListDto(Cuenta cuenta);

    List<CuentaListDto> toListDtoList(List<Cuenta> cuentas);
}
