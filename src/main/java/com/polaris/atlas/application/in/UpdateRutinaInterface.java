package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Rutina;

public interface UpdateRutinaInterface {
    Rutina update(Long usuarioId, Long id, Rutina rutina);
}
