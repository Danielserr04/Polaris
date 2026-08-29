package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.Entrada;

public interface CreateEntradaInterface {
    Entrada create(Long usuarioId, Entrada entrada);
}
