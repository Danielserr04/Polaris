package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.NotificacionFilter;

import java.util.List;

public interface ListNotificacionInterface {
    List<Notificacion> list(Long usuarioId, NotificacionFilter filter);
}
