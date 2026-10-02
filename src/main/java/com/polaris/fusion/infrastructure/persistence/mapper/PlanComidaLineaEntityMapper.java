package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.PlanComidaLinea;
import com.polaris.fusion.infrastructure.persistence.PlanComidaLineaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Al leer, alimento y receta se traducen a su modelo de dominio (la receta con
 * sus ingredientes) ademas de los ids planos.
 */
@Mapper(componentModel = "spring", uses = {AlimentoEntityMapper.class, RecetaEntityMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PlanComidaLineaEntityMapper {

    @Mapping(target = "alimentoId", source = "alimento.id")
    @Mapping(target = "recetaId", source = "receta.id")
    PlanComidaLinea toDomain(PlanComidaLineaEntity entity);

    /** alimento, receta y plan los pone PlanComidaJpaAdapter. */
    @Mapping(target = "alimento", ignore = true)
    @Mapping(target = "receta", ignore = true)
    @Mapping(target = "plan", ignore = true)
    PlanComidaLineaEntity toEntity(PlanComidaLinea domain);

    List<PlanComidaLinea> toDomainList(List<PlanComidaLineaEntity> entities);

    List<PlanComidaLineaEntity> toEntityList(List<PlanComidaLinea> domain);
}
