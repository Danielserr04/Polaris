package com.polaris.kuiper.application.in;

/**
 * Borra todas las leidas del usuario. Devuelve cuantas ha borrado.
 */
public interface BorrarNotificacionesLeidasInterface {
    int borrarLeidas(Long usuarioId);
}
