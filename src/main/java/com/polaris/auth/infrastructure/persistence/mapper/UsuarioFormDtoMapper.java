package com.polaris.auth.infrastructure.persistence.mapper;

import com.polaris.auth.domain.model.Usuario;
import com.polaris.auth.infrastructure.persistence.dto.out.UsuarioFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UsuarioFormDtoMapper {

    /**
     * tieneGoogle y tienePassword se derivan: al DTO sale si existen, nunca su
     * valor. Ver UsuarioFormDto.
     */
    @Mapping(target = "tieneGoogle", expression = "java(usuario.getGoogleId() != null)")
    @Mapping(target = "tienePassword", expression = "java(usuario.getPasswordHash() != null)")
    UsuarioFormDto toFormDto(Usuario usuario);
}
