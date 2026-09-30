package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Comida;

public interface CreateComidaInterface {
    Comida create(Long usuarioId, Comida comida);
}
