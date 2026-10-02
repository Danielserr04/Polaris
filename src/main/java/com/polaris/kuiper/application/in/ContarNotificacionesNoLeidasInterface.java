package com.polaris.kuiper.application.in;

/**
 * Para el contador de la campana: el frontend lo pide cada minuto.
 */
public interface ContarNotificacionesNoLeidasInterface {
    long contarNoLeidas(Long usuarioId);
}
