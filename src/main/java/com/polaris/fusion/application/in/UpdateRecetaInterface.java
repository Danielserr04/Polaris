package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Receta;

public interface UpdateRecetaInterface {
    Receta update(Long usuarioId, Long id, Receta receta);
}
