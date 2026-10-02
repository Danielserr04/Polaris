package com.polaris.odisea.application.out;

import com.polaris.odisea.domain.model.EstadisticasLogros;

/** Lo que necesitan los logros, sin cargar entradas ni titulos enteros. */
public interface EstadisticasLogrosPort {

    /** Solo lo del usuario. Con las listas vacias si no tiene nada. */
    EstadisticasLogros find(Long usuarioId);
}
