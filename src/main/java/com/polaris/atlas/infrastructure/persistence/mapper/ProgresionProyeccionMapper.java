package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.ProgresionSesion;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroRepository.ProgresionFila;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** De la fila agregada que devuelve la base al modelo de dominio. */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProgresionProyeccionMapper {

    ProgresionSesion toDomain(ProgresionFila fila);

    List<ProgresionSesion> toDomainList(List<ProgresionFila> filas);
}
