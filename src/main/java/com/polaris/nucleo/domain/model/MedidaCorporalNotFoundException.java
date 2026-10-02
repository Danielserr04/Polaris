package com.polaris.nucleo.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien se lanza cuando el id existe pero pertenece a
 * otro usuario: un 403 confirmaria que ese id existe.
 */
public class MedidaCorporalNotFoundException extends NotFoundException {

    public MedidaCorporalNotFoundException(Long id) {
        super("Medida corporal no encontrada: " + id);
    }
}
