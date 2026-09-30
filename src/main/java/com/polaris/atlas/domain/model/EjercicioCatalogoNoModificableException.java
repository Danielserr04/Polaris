package com.polaris.atlas.domain.model;

import com.polaris.shared.error.ForbiddenException;

/**
 * Se traduce a 403. El ejercicio del catalogo compartido es visible para todos,
 * asi que decir que no se puede tocar no revela nada.
 */
public class EjercicioCatalogoNoModificableException extends ForbiddenException {

    public EjercicioCatalogoNoModificableException(Long id) {
        super("El ejercicio " + id + " es del catalogo y no se puede modificar ni borrar");
    }
}
