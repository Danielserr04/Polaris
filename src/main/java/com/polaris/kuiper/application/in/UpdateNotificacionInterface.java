package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Notificacion;

/**
 * Solo cambia {@code leida}: el resto de la notificacion no se edita.
 */
public interface UpdateNotificacionInterface {
    Notificacion update(Long usuarioId, Long id, Notificacion cambios);
}
