package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.RegistroPeso;

/**
 * Un peso por dia: si el usuario ya tiene registro en esa fecha, se
 * actualiza en vez de crear otro. Ver docs/decisiones/010-registro-peso-un-peso-por-dia.md.
 */
public interface CreateRegistroPesoInterface {
    RegistroPeso create(Long usuarioId, RegistroPeso registro);
}
