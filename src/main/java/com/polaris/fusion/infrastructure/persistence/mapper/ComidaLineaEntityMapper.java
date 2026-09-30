package com.polaris.fusion.infrastructure.persistence.mapper;

import com.polaris.fusion.domain.model.ComidaLinea;
import com.polaris.fusion.infrastructure.persistence.ComidaLineaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses AlimentoEntityMapper para que entity.alimento se traduzca al Alimento de
 * dominio anidado en la linea, ademas del alimentoId plano.
 */
@Mapper(componentModel = "spring", uses = AlimentoEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ComidaLineaEntityMapper {

    @Mapping(target = "alimentoId", source = "alimento.id")
    ComidaLinea toDomain(ComidaLineaEntity entity);

    /**
     * El alimento sale solo con el id: ComidaJpaAdapter lo sustituye por la
     * Entity completa, y pone la referencia a la comida (no se mapea aqui para
     * no crear un ciclo).
     */
    @Mapping(target = "alimento.id", source = "alimentoId")
    @Mapping(target = "comida", ignore = true)
    ComidaLineaEntity toEntity(ComidaLinea domain);

    List<ComidaLinea> toDomainList(List<ComidaLineaEntity> entities);

    List<ComidaLineaEntity> toEntityList(List<ComidaLinea> domain);
}
