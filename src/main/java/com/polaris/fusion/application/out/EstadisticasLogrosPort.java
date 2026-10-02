package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.EstadisticasLogros;

/** Lo que necesitan los logros, contado en la base: el dominio no recibe comidas ni recetas. */
public interface EstadisticasLogrosPort {

    /** Solo lo del usuario. Con todo vacio o a cero si no tiene nada. */
    EstadisticasLogros find(Long usuarioId);
}
