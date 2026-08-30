package com.polaris.auth.application.in;

public interface CambiarPasswordInterface {
    /** passwordActual va a null cuando la cuenta todavia no tiene contrasena. */
    void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva);
}
