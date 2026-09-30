package com.polaris.nucleo.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien se lanza cuando el id existe pero pertenece a
 * otro usuario: un 403 confirmaria que ese id existe.
 */
public class RegistroPesoNotFoundException extends NotFoundException {

    public RegistroPesoNotFoundException(Long id) {
        super("Registro de peso no encontrado: " + id);
    }
}
