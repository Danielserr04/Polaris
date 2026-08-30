package com.polaris.odisea.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien se lanza cuando el id existe pero pertenece a
 * otro usuario: un 403 confirmaria que ese id existe. Ver
 * docs/modulos/odisea.md.
 */
public class EntradaNotFoundException extends NotFoundException {

    public EntradaNotFoundException(Long id) {
        super("Entrada no encontrada: " + id);
    }
}
