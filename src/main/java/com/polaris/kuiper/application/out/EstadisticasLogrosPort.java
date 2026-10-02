package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.EstadisticasLogros;

/** Lo que necesitan los logros, sin cargar movimientos enteros. */
public interface EstadisticasLogrosPort {

    /** Solo lo del usuario y nada de la papelera. Con todo vacio o a cero si no tiene nada. */
    EstadisticasLogros find(Long usuarioId);
}
