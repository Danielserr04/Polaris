package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.Perfil;

/**
 * Crea el perfil si el usuario aun no tiene uno. Un perfil por usuario: no
 * hay un "create" aparte. Ver docs/decisiones/009-perfil-unico-por-usuario.md.
 */
public interface UpdatePerfilInterface {
    Perfil update(Long usuarioId, Perfil perfil);
}
