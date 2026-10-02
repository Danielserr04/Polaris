package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.RecetaIngrediente;
import com.polaris.fusion.infrastructure.persistence.RecetaIngredienteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses AlimentoEntityMapper para que entity.alimento se traduzca al Alimento de
 * dominio anidado, ademas del alimentoId plano. Como ComidaLineaEntityMapper.
 */
@Mapper(componentModel = "spring", uses = AlimentoEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecetaIngredienteEntityMapper {

    @Mapping(target = "alimentoId", source = "alimento.id")
    RecetaIngrediente toDomain(RecetaIngredienteEntity entity);

    /**
     * El alimento sale solo con el id: RecetaJpaAdapter lo sustituye por la
     * Entity completa y pone la referencia a la receta (aqui no, para no crear
     * un ciclo).
     */
    @Mapping(target = "alimento.id", source = "alimentoId")
    @Mapping(target = "receta", ignore = true)
    RecetaIngredienteEntity toEntity(RecetaIngrediente domain);

    List<RecetaIngrediente> toDomainList(List<RecetaIngredienteEntity> entities);

    List<RecetaIngredienteEntity> toEntityList(List<RecetaIngrediente> domain);
}
