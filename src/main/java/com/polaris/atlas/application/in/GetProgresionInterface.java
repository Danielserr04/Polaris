package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;

import java.util.List;

public interface GetProgresionInterface {

    /**
     * Evolucion de un ejercicio sesion a sesion, de la mas antigua a la mas
     * reciente. Lista vacia si el ejercicio no tiene series en ese rango.
     */
    List<ProgresionSesion> get(Long usuarioId, ProgresionFilter filter);
}
