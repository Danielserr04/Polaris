package com.polaris.shared.error;

/**
 * Recurso inexistente. Se traduce a 404.
 *
 * <p>Los modulos no la lanzan directamente: cada entidad define la suya
 * ({@code TituloNotFoundException}, {@code MovimientoNotFoundException}...)
 * extendiendo esta. Ver docs/convenciones.md.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String mensaje) {
        super(mensaje);
    }
}
