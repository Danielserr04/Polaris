package com.polaris.fusion.application.out;

import com.polaris.fusion.domain.model.DatosCorporales;

import java.util.Optional;

/**
 * Lo que Fusion necesita del perfil, en lenguaje de Fusion. El dato vive en
 * Nucleo y solo el adaptador de infrastructure/nucleo lo sabe, como con
 * PesoCorporalPort.
 */
public interface DatosCorporalesPort {

    /** Vacio si el usuario aun no ha guardado su perfil. */
    Optional<DatosCorporales> find(Long usuarioId);
}
