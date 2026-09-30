package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.infrastructure.persistence.PresupuestoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses CategoriaEntityMapper para que entity.categoria (CategoriaEntity) se
 * traduzca a la Categoria de dominio anidada dentro de Presupuesto, ademas del
 * categoriaId plano.
 */
@Mapper(componentModel = "spring", uses = CategoriaEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PresupuestoEntityMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    Presupuesto toDomain(PresupuestoEntity entity);

    /** categoria se ignora aqui: PresupuestoJpaAdapter la pone a mano con la Entity completa. */
    @Mapping(target = "categoria", ignore = true)
    PresupuestoEntity toEntity(Presupuesto domain);

    List<Presupuesto> toDomainList(List<PresupuestoEntity> entities);
}
