package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.EstadisticasEntreno;

/**
 * Los totales de entreno se agregan en la base: el dominio nunca recibe las
 * sesiones ni las series para contarlas.
 */
public interface EstadisticasEntrenoPort {

    /** Solo cuenta lo del usuario. Con todo a cero si no ha entrenado nunca. */
    EstadisticasEntreno find(Long usuarioId);
}
