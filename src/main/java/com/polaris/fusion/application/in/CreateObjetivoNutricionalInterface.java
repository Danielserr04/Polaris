package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.ObjetivoNutricional;

/**
 * Crea un objetivo nuevo con su vigente_desde; nunca sustituye a uno anterior.
 * Ver docs/decisiones/016-objetivo-nutricional-historico-inmutable.md.
 */
public interface CreateObjetivoNutricionalInterface {
    ObjetivoNutricional create(Long usuarioId, ObjetivoNutricional objetivo);
}
