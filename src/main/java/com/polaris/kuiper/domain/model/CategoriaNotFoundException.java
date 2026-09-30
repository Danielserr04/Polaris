package com.polaris.kuiper.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien se lanza cuando el id existe pero pertenece a
 * otro usuario: un 403 confirmaria que ese id existe.
 */
public class CategoriaNotFoundException extends NotFoundException {

    public CategoriaNotFoundException(Long id) {
        super("Categoria no encontrada: " + id);
    }
}
