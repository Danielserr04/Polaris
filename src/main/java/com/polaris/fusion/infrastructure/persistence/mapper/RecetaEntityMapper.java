package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.infrastructure.persistence.RecetaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = RecetaIngredienteEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecetaEntityMapper {

    Receta toDomain(RecetaEntity entity);

    RecetaEntity toEntity(Receta domain);

    List<Receta> toDomainList(List<RecetaEntity> entities);
}
