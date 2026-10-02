package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Notificacion;

public interface GetNotificacionInterface {
    Notificacion get(Long usuarioId, Long id);
}
