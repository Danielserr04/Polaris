package com.polaris.nucleo.domain.model;

import com.polaris.shared.error.NotFoundException;

/**
 * Se traduce a 404. Se lanza cuando el usuario aun no ha guardado su perfil.
 */
public class PerfilNotFoundException extends NotFoundException {

    public PerfilNotFoundException(Long usuarioId) {
        super("Perfil no encontrado para el usuario: " + usuarioId);
    }
}
