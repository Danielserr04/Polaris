package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.infrastructure.persistence.dto.out.EntradaListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EntradaListDtoMapper {

    @Mapping(target = "tituloTitulo", source = "titulo.titulo")
    @Mapping(target = "tituloImagenUrl", source = "titulo.imagenUrl")
    @Mapping(target = "tituloTipo", source = "titulo.tipo")
    EntradaListDto toListDto(Entrada entrada);

    List<EntradaListDto> toListDtoList(List<Entrada> entradas);
}
