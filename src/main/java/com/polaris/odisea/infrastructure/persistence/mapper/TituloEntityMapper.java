package com.polaris.odisea.infrastructure.persistence.mapper;

import com.polaris.odisea.domain.model.Titulo;
import com.polaris.odisea.infrastructure.persistence.TituloEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TituloEntityMapper {

    Titulo toDomain(TituloEntity entity);

    TituloEntity toEntity(Titulo domain);

    List<Titulo> toDomainList(List<TituloEntity> entities);
}
