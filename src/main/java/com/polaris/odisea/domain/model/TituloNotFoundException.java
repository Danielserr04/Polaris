package com.polaris.odisea.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404 en el GlobalExceptionHandler de shared/.
 */
public class TituloNotFoundException extends NotFoundException {

    public TituloNotFoundException(Long id) {
        super("Titulo no encontrado: " + id);
    }
}
