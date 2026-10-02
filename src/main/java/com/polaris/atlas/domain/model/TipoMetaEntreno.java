package com.polaris.atlas.domain.model;

/**
 * Que mide una meta de entreno. Cada tipo saca su valor actual de un sitio
 * distinto. Ver docs/decisiones/043-logros-calculados-y-metas.md.
 */
public enum TipoMetaEntreno {
    /** Llegar a un peso corporal, en kg, bajando o subiendo. Valor actual: el ultimo peso. */
    PESO_CORPORAL,
    /** Levantar un peso en una serie de un ejercicio, en kg. Valor actual: su record de peso. */
    MARCA_EJERCICIO,
    /** Entrenar N dias por semana. Valor actual: sesiones de la semana en curso. */
    SESIONES_SEMANA
}
