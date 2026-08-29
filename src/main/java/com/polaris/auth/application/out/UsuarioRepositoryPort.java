package com.polaris.auth.application.out;

import com.polaris.auth.domain.model.Usuario;

import java.util.Optional;

/**
 * Lo que el dominio necesita de la persistencia, en lenguaje de dominio.
 * Habla de Usuario, nunca de UsuarioEntity.
 */
public interface UsuarioRepositoryPort {

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByGoogleId(String googleId);
}
