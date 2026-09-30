package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Rutina;

public interface CreateRutinaInterface {
    Rutina create(Long usuarioId, Rutina rutina);
}
