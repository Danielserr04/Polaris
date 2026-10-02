package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Receta;

public interface GetRecetaInterface {
    Receta get(Long usuarioId, Long id);
}
