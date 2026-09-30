package com.polaris.atlas.infrastructure.persistence.mapper;

import com.polaris.atlas.domain.model.MejorPesoEjercicio;
import com.polaris.atlas.domain.model.VolumenSesionEjercicio;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroRepository.MejorPesoFila;
import com.polaris.atlas.infrastructure.persistence.SerieRegistroRepository.VolumenSesionFila;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** De las filas agregadas que devuelve la base a los modelos de dominio de los records. */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecordProyeccionMapper {

    MejorPesoEjercicio toDomain(MejorPesoFila fila);

    List<MejorPesoEjercicio> toMejorPesoList(List<MejorPesoFila> filas);

    VolumenSesionEjercicio toDomain(VolumenSesionFila fila);

    List<VolumenSesionEjercicio> toVolumenList(List<VolumenSesionFila> filas);
}
