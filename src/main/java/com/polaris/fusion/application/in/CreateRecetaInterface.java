package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Receta;

public interface CreateRecetaInterface {
    Receta create(Long usuarioId, Receta receta);
}
