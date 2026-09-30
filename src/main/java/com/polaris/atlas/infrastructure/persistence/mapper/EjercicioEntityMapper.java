package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.infrastructure.persistence.EjercicioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EjercicioEntityMapper {

    Ejercicio toDomain(EjercicioEntity entity);

    EjercicioEntity toEntity(Ejercicio domain);

    List<Ejercicio> toDomainList(List<EjercicioEntity> entities);
}
