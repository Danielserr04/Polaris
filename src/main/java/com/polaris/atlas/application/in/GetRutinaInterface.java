package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Rutina;

public interface GetRutinaInterface {
    Rutina get(Long usuarioId, Long id);
}
