package com.polaris.kuiper.infrastructure.persistence.mapper;

import com.polaris.kuiper.domain.model.AportacionMeta;
import com.polaris.kuiper.domain.model.MetaAhorro;
import com.polaris.kuiper.infrastructure.persistence.AportacionMetaEntity;
import com.polaris.kuiper.infrastructure.persistence.MetaAhorroEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Meta y aportacion en un solo mapper, como el puerto: la aportacion no
 * existe fuera de su meta. Los campos calculados de MetaAhorro los pone el
 * adaptador (importeActual) o el servicio (plazo).
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaAhorroEntityMapper {

    @Mapping(target = "importeActual", ignore = true)
    @Mapping(target = "diasRestantes", ignore = true)
    @Mapping(target = "ahorroMensualNecesario", ignore = true)
    MetaAhorro toDomain(MetaAhorroEntity entity);

    MetaAhorroEntity toEntity(MetaAhorro domain);

    AportacionMeta toDomain(AportacionMetaEntity entity);

    AportacionMetaEntity toEntity(AportacionMeta domain);

    List<AportacionMeta> toAportacionList(List<AportacionMetaEntity> entities);
}
