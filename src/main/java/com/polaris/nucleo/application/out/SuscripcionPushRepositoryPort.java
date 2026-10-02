package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.SuscripcionPush;

import java.util.List;
import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de SuscripcionPush, nunca de SuscripcionPushEntity.
 */
public interface SuscripcionPushRepositoryPort {

    SuscripcionPush save(SuscripcionPush suscripcion);

    /** El endpoint es unico en toda la tabla: identifica al dispositivo. */
    Optional<SuscripcionPush> findByEndpoint(String endpoint);

    List<SuscripcionPush> findAllByUsuarioId(Long usuarioId);

    /** Usuarios con al menos un dispositivo suscrito. Para la pasada del job. */
    List<Long> findUsuarioIds();

    void deleteById(Long id);
}
