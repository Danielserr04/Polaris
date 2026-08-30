package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.ResultadoCatalogo;
import com.polaris.odisea.domain.model.TipoContenido;

import java.util.List;

public interface BuscarCatalogoInterface {
    List<ResultadoCatalogo> buscar(String texto, TipoContenido tipo);
}
