package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Comida;
import com.polaris.fusion.domain.model.ComidaFilter;

import java.util.List;

public interface ListComidaInterface {
    List<Comida> list(Long usuarioId, ComidaFilter filter);
}
