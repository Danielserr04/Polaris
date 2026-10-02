package com.polaris.nucleo.application.in;

/** Deja de mandar avisos a ese dispositivo. Por endpoint: es lo que conoce el navegador. */
public interface DeleteSuscripcionPushInterface {
    void delete(Long usuarioId, String endpoint);
}
