package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.MetaEntreno;
import com.polaris.atlas.infrastructure.persistence.MetaEntrenoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MetaEntrenoEntityMapper {

    /** Lo calculado lo rellena el servicio al leer. */
    @Mapping(target = "ejercicioNombre", ignore = true)
    @Mapping(target = "valorActual", ignore = true)
    @Mapping(target = "progresoPct", ignore = true)
    @Mapping(target = "conseguida", ignore = true)
    MetaEntreno toDomain(MetaEntrenoEntity entity);

    MetaEntrenoEntity toEntity(MetaEntreno domain);

    List<MetaEntreno> toDomainList(List<MetaEntrenoEntity> entities);
}
