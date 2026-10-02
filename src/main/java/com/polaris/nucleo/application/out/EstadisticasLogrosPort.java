package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.EstadisticasLogros;

/** Las fechas que necesitan los logros, sin cargar pesos ni medidas enteros. */
public interface EstadisticasLogrosPort {

    /** Solo lo del usuario. Con las listas vacias y 0 campos si no tiene nada. */
    EstadisticasLogros find(Long usuarioId);
}
