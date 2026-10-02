package com.polaris.kuiper.application.out;

import com.polaris.kuiper.domain.model.Notificacion;
import com.polaris.kuiper.domain.model.NotificacionFilter;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Notificacion, nunca de NotificacionEntity.
 */
public interface NotificacionRepositoryPort {

    /**
     * Guarda en su propia transaccion: si falla (por ejemplo, el unique
     * (usuario_id, clave) en una carrera) no arrastra a la transaccion de
     * quien la llama.
     */
    Notificacion save(Notificacion notificacion);

    Optional<Notificacion> findById(Long id);

    /** Mas reciente primero. */
    List<Notificacion> findAll(Long usuarioId, NotificacionFilter filter);

    void deleteById(Long id);

    /** Para no avisar dos veces del mismo hecho. El unique (usuario_id, clave) es la red. */
    boolean existsByUsuarioIdAndClave(Long usuarioId, String clave);

    long countNoLeidas(Long usuarioId);

    /** Devuelve cuantas ha marcado. */
    int marcarTodasLeidas(Long usuarioId);

    /** Devuelve cuantas ha borrado. */
    int deleteLeidas(Long usuarioId);
}
