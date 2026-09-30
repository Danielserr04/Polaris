package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Comida;

public interface UpdateComidaInterface {
    Comida update(Long usuarioId, Long id, Comida comida);
}
