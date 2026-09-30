package com.polaris.atlas.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien se lanza cuando el id existe pero pertenece a
 * otro usuario: un 403 confirmaria que ese id existe.
 */
public class SesionNotFoundException extends NotFoundException {

    public SesionNotFoundException(Long id) {
        super("Sesion no encontrada: " + id);
    }
}
