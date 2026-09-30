package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Sesion;

public interface GetSesionInterface {
    Sesion get(Long usuarioId, Long id);
}
