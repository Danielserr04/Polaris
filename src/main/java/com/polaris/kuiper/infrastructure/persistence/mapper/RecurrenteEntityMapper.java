package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Recurrente;
import com.polaris.kuiper.infrastructure.persistence.RecurrenteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses CategoriaEntityMapper para la Categoria anidada, como en MovimientoEntityMapper.
 */
@Mapper(componentModel = "spring", uses = CategoriaEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecurrenteEntityMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    Recurrente toDomain(RecurrenteEntity entity);

    /** categoria se ignora aqui: RecurrenteJpaAdapter la pone a mano con la Entity completa. */
    @Mapping(target = "categoria", ignore = true)
    RecurrenteEntity toEntity(Recurrente domain);

    List<Recurrente> toDomainList(List<RecurrenteEntity> entities);
}
