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
@Mapper(componentModel = "spring", uses = {CategoriaEntityMapper.class, CuentaEntityMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecurrenteEntityMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "cuentaId", source = "cuenta.id")
    Recurrente toDomain(RecurrenteEntity entity);

    /** categoria y cuenta se ignoran aqui: RecurrenteJpaAdapter las pone a mano con las Entity completas. */
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cuenta", ignore = true)
    RecurrenteEntity toEntity(Recurrente domain);

    List<Recurrente> toDomainList(List<RecurrenteEntity> entities);
}
