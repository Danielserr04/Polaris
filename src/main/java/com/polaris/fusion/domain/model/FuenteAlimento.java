package com.polaris.fusion.domain.model;

/**
 * De donde sale la ficha. MANUAL o importada de Open Food Facts (ver
 * docs/decisiones/018-alimentos-open-food-facts.md). Cada valor nuevo exige una
 * migracion, porque fuente_externa es un ENUM de MySQL (como en V2 y V11).
 */
public enum FuenteAlimento {
    MANUAL,
    OPEN_FOOD_FACTS
}
