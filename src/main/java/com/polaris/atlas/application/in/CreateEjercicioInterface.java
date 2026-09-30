package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Ejercicio;

public interface CreateEjercicioInterface {
    Ejercicio create(Long usuarioId, Ejercicio ejercicio);
}
