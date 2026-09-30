package com.polaris.atlas.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Tambien se lanza cuando el id existe pero es el ejercicio
 * propio de otro usuario: un 403 confirmaria que ese id existe.
 */
public class EjercicioNotFoundException extends NotFoundException {

    public EjercicioNotFoundException(Long id) {
        super("Ejercicio no encontrado: " + id);
    }
}
