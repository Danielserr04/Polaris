package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.domain.model.TrabajoMuscularFilter;

import java.util.List;

public interface GetTrabajoMuscularInterface {

    /**
     * Cuanto se ha trabajado cada grupo muscular en un rango de fechas, del
     * grupo con mas series al que menos. Lista vacia si no hay series.
     */
    List<TrabajoMuscular> get(Long usuarioId, TrabajoMuscularFilter filter);
}
