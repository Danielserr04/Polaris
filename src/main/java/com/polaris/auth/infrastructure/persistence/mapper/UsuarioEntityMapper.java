package com.polaris.auth.infrastructure.persistence.mapper;

import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UsuarioEntityMapper {

    Usuario toDomain(UsuarioEntity entity);

    UsuarioEntity toEntity(Usuario domain);
}
