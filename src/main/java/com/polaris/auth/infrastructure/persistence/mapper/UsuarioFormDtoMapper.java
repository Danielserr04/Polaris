package com.polaris.auth.infrastructure.persistence.mapper;

import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.dto.out.UsuarioFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UsuarioFormDtoMapper {

    UsuarioFormDto toFormDto(Usuario usuario);
}
