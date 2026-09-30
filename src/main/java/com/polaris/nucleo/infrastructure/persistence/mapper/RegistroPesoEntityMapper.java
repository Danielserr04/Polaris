package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.RegistroPeso;
import com.polaris.nucleo.infrastructure.persistence.RegistroPesoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RegistroPesoEntityMapper {

    RegistroPeso toDomain(RegistroPesoEntity entity);

    RegistroPesoEntity toEntity(RegistroPeso domain);

    List<RegistroPeso> toDomainList(List<RegistroPesoEntity> entities);
}
