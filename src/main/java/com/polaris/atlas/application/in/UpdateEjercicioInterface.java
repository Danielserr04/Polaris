package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Ejercicio;

public interface UpdateEjercicioInterface {
    Ejercicio update(Long usuarioId, Long id, Ejercicio ejercicio);
}
