package com.polaris.fusion.domain.model;

/**
 * De donde sale la ficha. Hoy solo MANUAL: la API de alimentos esta sin
 * elegir (docs/modulos/fusion.md). Cuando se elija, su valor entra aqui con
 * una migracion nueva, porque fuente_externa es un ENUM de MySQL (como en V2).
 */
public enum FuenteAlimento {
    MANUAL
}
