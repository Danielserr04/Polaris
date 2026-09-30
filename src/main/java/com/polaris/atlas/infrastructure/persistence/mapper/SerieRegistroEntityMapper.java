package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.SerieRegistro;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * uses EjercicioEntityMapper para que entity.ejercicio se traduzca al Ejercicio
 * de dominio anidado en la serie, ademas del ejercicioId plano.
 */
@Mapper(componentModel = "spring", uses = EjercicioEntityMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SerieRegistroEntityMapper {

    @Mapping(target = "ejercicioId", source = "ejercicio.id")
    SerieRegistro toDomain(SerieRegistroEntity entity);

    /**
     * El ejercicio sale solo con el id: SesionJpaAdapter lo sustituye por la
     * Entity completa, y pone la referencia a la sesion (no se mapea aqui para
     * no crear un ciclo).
     */
    @Mapping(target = "ejercicio.id", source = "ejercicioId")
    @Mapping(target = "sesion", ignore = true)
    SerieRegistroEntity toEntity(SerieRegistro domain);

    List<SerieRegistro> toDomainList(List<SerieRegistroEntity> entities);

    List<SerieRegistroEntity> toEntityList(List<SerieRegistro> domain);
}
