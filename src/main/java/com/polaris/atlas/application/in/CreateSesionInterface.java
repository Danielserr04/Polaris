package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Sesion;

public interface CreateSesionInterface {
    Sesion create(Long usuarioId, Sesion sesion);
}
