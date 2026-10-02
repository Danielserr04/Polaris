package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Notificacion;

import java.util.Optional;

/**
 * Lo llaman otros servicios del modulo, no un endpoint. Idempotente por
 * (usuarioId, clave): si ya existe una notificacion con esa clave no crea
 * otra. Nunca lanza: si algo falla lo deja en el log y devuelve vacio,
 * porque un aviso no puede tumbar la operacion que lo provoca.
 *
 * @return la notificacion creada, o vacio si ya existia o no se pudo crear
 */
public interface CrearNotificacionInterface {
    Optional<Notificacion> crear(Notificacion notificacion);
}
