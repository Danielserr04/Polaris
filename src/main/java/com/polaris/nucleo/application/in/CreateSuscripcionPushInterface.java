package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.SuscripcionPush;

/**
 * Da de alta el dispositivo, o lo actualiza si su endpoint ya estaba (el
 * navegador repite la suscripcion al volver a pedir permiso).
 */
public interface CreateSuscripcionPushInterface {
    SuscripcionPush create(Long usuarioId, SuscripcionPush suscripcion);
}
