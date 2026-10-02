package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.Transferencia;
import com.polaris.kuiper.infrastructure.persistence.TransferenciaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses CuentaEntityMapper para las dos Cuenta anidadas, como la Categoria en
 * MovimientoEntityMapper.
 */
@Mapper(componentModel = "spring", uses = CuentaEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferenciaEntityMapper {

    @Mapping(target = "cuentaOrigenId", source = "cuentaOrigen.id")
    @Mapping(target = "cuentaDestinoId", source = "cuentaDestino.id")
    Transferencia toDomain(TransferenciaEntity entity);

    /** Las cuentas se ignoran aqui: TransferenciaJpaAdapter las pone a mano con las Entity completas. */
    @Mapping(target = "cuentaOrigen", ignore = true)
    @Mapping(target = "cuentaDestino", ignore = true)
    TransferenciaEntity toEntity(Transferencia domain);

    List<Transferencia> toDomainList(List<TransferenciaEntity> entities);
}
