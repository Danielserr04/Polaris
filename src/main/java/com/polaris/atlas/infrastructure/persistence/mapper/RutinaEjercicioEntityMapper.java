package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.RutinaEjercicio;
import com.polaris.atlas.infrastructure.persistence.RutinaEjercicioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses EjercicioEntityMapper para que entity.ejercicio se traduzca al Ejercicio
 * de dominio anidado en la linea, ademas del ejercicioId plano.
 */
@Mapper(componentModel = "spring", uses = EjercicioEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RutinaEjercicioEntityMapper {

    @Mapping(target = "ejercicioId", source = "ejercicio.id")
    RutinaEjercicio toDomain(RutinaEjercicioEntity entity);

    /**
     * El ejercicio sale solo con el id: RutinaJpaAdapter lo sustituye por la
     * Entity completa, y pone la referencia a la rutina (no se mapea aqui para
     * no crear un ciclo).
     */
    @Mapping(target = "ejercicio.id", source = "ejercicioId")
    @Mapping(target = "rutina", ignore = true)
    RutinaEjercicioEntity toEntity(RutinaEjercicio domain);

    List<RutinaEjercicio> toDomainList(List<RutinaEjercicioEntity> entities);

    List<RutinaEjercicioEntity> toEntityList(List<RutinaEjercicio> domain);
}
