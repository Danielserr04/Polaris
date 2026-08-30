package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaFormDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EntradaFormDtoMapper {

    @Mapping(target = "tituloTitulo", source = "titulo.titulo")
    @Mapping(target = "tituloImagenUrl", source = "titulo.imagenUrl")
    @Mapping(target = "tituloTipo", source = "titulo.tipo")
    EntradaFormDto toFormDto(Entrada entrada);
}
