package com.polaris.shared.error;

/**
 * Identidad valida, pero la accion esta bloqueada por una regla de negocio.
 * Se traduce a 403.
 *
 * <p>Los modulos no la lanzan directamente: cada caso define la suya
 * extendiendo esta. Ver docs/convenciones.md.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String mensaje) {
        super(mensaje);
    }
}
