package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.EstadisticasEntreno;

/**
 * Los totales de entreno se agregan en la base: el dominio nunca recibe las
 * sesiones ni las series para contarlas.
 */
public interface EstadisticasEntrenoPort {

    /** Solo lo del usuario. Con las listas vacias si no ha entrenado nunca. */
    EstadisticasEntreno find(Long usuarioId);
}
