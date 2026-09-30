package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.Rutina;
import com.polaris.atlas.domain.model.RutinaFilter;

import java.util.List;

public interface ListRutinaInterface {
    List<Rutina> list(Long usuarioId, RutinaFilter filter);
}
