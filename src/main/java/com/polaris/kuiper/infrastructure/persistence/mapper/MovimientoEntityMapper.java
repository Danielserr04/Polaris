package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.infrastructure.persistence.MovimientoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses CategoriaEntityMapper para que entity.categoria (CategoriaEntity) se
 * traduzca a la Categoria de dominio anidada dentro de Movimiento, ademas del
 * categoriaId plano.
 */
@Mapper(componentModel = "spring", uses = {CategoriaEntityMapper.class, CuentaEntityMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MovimientoEntityMapper {

    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "cuentaId", source = "cuenta.id")
    Movimiento toDomain(MovimientoEntity entity);

    /** categoria y cuenta se ignoran aqui: MovimientoJpaAdapter las pone a mano con las Entity completas. */
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cuenta", ignore = true)
    MovimientoEntity toEntity(Movimiento domain);

    List<Movimiento> toDomainList(List<MovimientoEntity> entities);
}
