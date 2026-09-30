package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Ejercicio;
import com.polaris.atlas.domain.model.EjercicioFilter;

import java.util.List;

public interface ListEjercicioInterface {
    List<Ejercicio> list(Long usuarioId, EjercicioFilter filter);
}
