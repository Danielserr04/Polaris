package com.polaris.nucleo.application.out;

import com.polaris.nucleo.domain.model.Perfil;

import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Perfil, nunca de PerfilEntity.
 *
 * <p>Se busca por usuarioId, no por id: hay un perfil por usuario.
 */
public interface PerfilRepositoryPort {

    Perfil save(Perfil perfil);

    Optional<Perfil> findByUsuarioId(Long usuarioId);
}
