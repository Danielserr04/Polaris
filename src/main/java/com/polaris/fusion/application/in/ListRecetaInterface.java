package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Receta;
import com.polaris.fusion.domain.model.RecetaFilter;

import java.util.List;

public interface ListRecetaInterface {
    List<Receta> list(Long usuarioId, RecetaFilter filter);
}
