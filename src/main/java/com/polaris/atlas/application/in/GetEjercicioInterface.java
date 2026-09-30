package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Ejercicio;

public interface GetEjercicioInterface {
    Ejercicio get(Long usuarioId, Long id);
}
