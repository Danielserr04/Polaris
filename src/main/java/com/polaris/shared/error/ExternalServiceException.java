package com.polaris.shared.error;

/**
 * Una API externa ha fallado o no ha contestado. Se traduce a 502.
 *
 * <p>No es un 500: el fallo no esta en Polaris. Distinguirlo importa para saber
 * si mirar los logs propios o el estado de TMDB.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String mensaje) {
        super(mensaje);
    }

    public ExternalServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
