package com.polaris.auth.domain.model;

import com.polaris.shared.error.UnauthorizedException;

/**
 * Se traduce a 401. Mismo mensaje tanto si el usuario no existe como si la
 * contrasena falla: distinguirlos le diria a cualquiera que correos estan
 * dados de alta.
 */
public class CredencialesInvalidasException extends UnauthorizedException {

    public CredencialesInvalidasException() {
        super("Usuario o contrasena incorrectos");
    }
}
