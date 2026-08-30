package com.polaris.odisea.application.in;

import com.polaris.odisea.domain.model.Entrada;
import com.polaris.odisea.domain.model.TipoContenido;

public interface ImportarEntradaInterface {
    Entrada importar(Long usuarioId, String idExterno, TipoContenido tipo);
}
