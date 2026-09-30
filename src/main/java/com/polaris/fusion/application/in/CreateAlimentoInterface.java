package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.Alimento;

/**
 * Crea un alimento a mano. Los que vienen de una API externa se importaran por
 * su propio caso de uso, no por aqui. Ver docs/decisiones/015-alimento-catalogo-compartido-macros-por-100g.md.
 */
public interface CreateAlimentoInterface {
    Alimento create(Alimento alimento);
}
