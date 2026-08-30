package com.polaris.auth.application.out;

/**
 * Enviar el enlace de verificacion de email. Dos adaptadores por perfil: en
 * dev escribe el enlace en el log (sin SMTP, sin dependencia nueva), en prod
 * lo manda por correo de verdad. Ver docs/modulos/auth.md.
 */
public interface EnviarVerificacionPort {

    void enviar(String email, String nombre, String tokenVerificacion);
}
