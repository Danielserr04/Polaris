package com.polaris.fusion.domain.model;

/**
 * Resultado de importar: el Alimento y si se acaba de crear o ya existia.
 * El controller lo usa para responder 201 o 200.
 */
public record ImportacionAlimento(Alimento alimento, boolean creado) {
}
