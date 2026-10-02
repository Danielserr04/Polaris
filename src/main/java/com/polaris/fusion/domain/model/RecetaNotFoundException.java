package com.polaris.fusion.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien se lanza cuando el id existe pero pertenece a
 * otro usuario: un 403 confirmaria que ese id existe.
 */
public class RecetaNotFoundException extends NotFoundException {

    public RecetaNotFoundException(Long id) {
        super("Receta no encontrada: " + id);
    }
}
