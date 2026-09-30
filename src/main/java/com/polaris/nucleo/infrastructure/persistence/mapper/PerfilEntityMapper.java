package com.polaris.nucleo.infrastructure.persistence.mapper;

import com.polaris.nucleo.domain.model.Perfil;
import com.polaris.nucleo.infrastructure.persistence.PerfilEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PerfilEntityMapper {

    Perfil toDomain(PerfilEntity entity);

    PerfilEntity toEntity(Perfil domain);
}
