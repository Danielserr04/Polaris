package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.Entrada;

public interface UpdateEntradaInterface {
    Entrada update(Long usuarioId, Long id, Entrada entrada);
}
