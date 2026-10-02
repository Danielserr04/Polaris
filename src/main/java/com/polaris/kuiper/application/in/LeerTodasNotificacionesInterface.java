package com.polaris.kuiper.application.in;

/**
 * Marca como leidas todas las del usuario. Devuelve cuantas ha cambiado.
 */
public interface LeerTodasNotificacionesInterface {
    int leerTodas(Long usuarioId);
}
