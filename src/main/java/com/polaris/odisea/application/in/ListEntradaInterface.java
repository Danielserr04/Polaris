package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.EntradaFilter;

import java.util.List;

public interface ListEntradaInterface {
    List<Entrada> list(Long usuarioId, EntradaFilter filter);
}
