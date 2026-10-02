package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Cuenta;
import com.polaris.kuiper.infrastructure.persistence.CuentaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CuentaEntityMapper {

    /** saldoActual no esta en la tabla: lo calcula CuentaService. */
    @Mapping(target = "saldoActual", ignore = true)
    Cuenta toDomain(CuentaEntity entity);

    CuentaEntity toEntity(Cuenta domain);

    List<Cuenta> toDomainList(List<CuentaEntity> entities);
}
