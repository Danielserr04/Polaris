package com.polaris.shared.error;

/**
 * Recurso que ya existe y no admite duplicado. Se traduce a 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }
}
