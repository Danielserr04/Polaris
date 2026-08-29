package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.infrastructure.persistence.EntradaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses TituloEntityMapper para que entity.titulo (TituloEntity) se traduzca
 * al Titulo de dominio anidado dentro de Entrada, ademas del tituloId plano.
 */
@Mapper(componentModel = "spring", uses = TituloEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EntradaEntityMapper {

    @Mapping(target = "tituloId", source = "titulo.id")
    Entrada toDomain(EntradaEntity entity);

    /** titulo se ignora aqui: EntradaJpaAdapter lo pone a mano con la Entity completa. */
    @Mapping(target = "titulo", ignore = true)
    EntradaEntity toEntity(Entrada domain);

    List<Entrada> toDomainList(List<EntradaEntity> entities);
}
