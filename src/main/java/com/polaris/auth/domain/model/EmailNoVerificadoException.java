package com.polaris.auth.domain.model;

import com.polaris.shared.error.ForbiddenException;

/**
 * Credenciales correctas, pero el correo todavia no se ha verificado.
 * Se traduce a 403.
 */
public class EmailNoVerificadoException extends ForbiddenException {

    public EmailNoVerificadoException() {
        super("Verifica tu correo antes de iniciar sesion");
    }
}
