package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.RecordEjercicio;

import java.util.List;

public interface ListRecordsInterface {

    /** Las mejores marcas del usuario, una fila por ejercicio con series, ordenadas por nombre. */
    List<RecordEjercicio> list(Long usuarioId);
}
