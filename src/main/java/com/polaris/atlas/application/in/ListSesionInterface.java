package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Sesion;
import com.polaris.atlas.domain.model.SesionFilter;

import java.util.List;

public interface ListSesionInterface {
    List<Sesion> list(Long usuarioId, SesionFilter filter);
}
