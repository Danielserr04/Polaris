package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.ObjetivoNutricional;

import java.util.List;

public interface ListObjetivoNutricionalInterface {

    /** Historico completo del usuario, el mas reciente primero. */
    List<ObjetivoNutricional> list(Long usuarioId);
}
