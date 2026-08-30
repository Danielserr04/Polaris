package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.Entrada;

public interface GetEntradaInterface {
    Entrada get(Long usuarioId, Long id);
}
