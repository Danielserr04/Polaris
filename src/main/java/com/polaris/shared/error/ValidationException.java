package com.polaris.shared.error;

/**
 * Regla de negocio incumplida. Se traduce a 400.
 *
 * <p>Para validaciones de formato de los DTOs de entrada estan las anotaciones
 * de Bean Validation; esta es para lo que solo sabe el dominio.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String mensaje) {
        super(mensaje);
    }
}
