package com.polaris.auth.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404 en el GlobalExceptionHandler de shared/.
 */
public class UsuarioNotFoundException extends NotFoundException {

    public UsuarioNotFoundException(Long id) {
        super("Usuario no encontrado: " + id);
    }
}
