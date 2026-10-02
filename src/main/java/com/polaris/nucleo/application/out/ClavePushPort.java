package com.polaris.nucleo.application.out;

/**
 * La clave publica VAPID del servidor, en base64url. La genera y la guarda la
 * infraestructura de push la primera vez que hace falta.
 */
public interface ClavePushPort {

    String clavePublica();
}
