package com.polaris.shared.error;

/**
 * Credenciales incorrectas o inexistentes. Se traduce a 401.
 *
 * <p>Los modulos no la lanzan directamente: cada caso define la suya
 * extendiendo esta. Ver docs/convenciones.md.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String mensaje) {
        super(mensaje);
    }
}
