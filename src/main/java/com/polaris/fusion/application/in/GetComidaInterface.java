package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Comida;

public interface GetComidaInterface {
    Comida get(Long usuarioId, Long id);
}
