package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroRepository.TrabajoMuscularFila;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** De la fila agregada que devuelve la base al modelo de dominio. */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TrabajoMuscularProyeccionMapper {

    TrabajoMuscular toDomain(TrabajoMuscularFila fila);

    List<TrabajoMuscular> toDomainList(List<TrabajoMuscularFila> filas);
}
