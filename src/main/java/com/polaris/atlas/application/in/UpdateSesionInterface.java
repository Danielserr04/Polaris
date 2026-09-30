package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Sesion;

public interface UpdateSesionInterface {
    Sesion update(Long usuarioId, Long id, Sesion sesion);
}
