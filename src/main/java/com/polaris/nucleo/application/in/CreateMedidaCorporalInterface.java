package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.MedidaCorporal;

/**
 * Una medicion por dia: si el usuario ya tiene registro en esa fecha, se
 * actualiza en vez de crear otro. Ver docs/decisiones/041-medida-corporal-una-por-dia.md.
 */
public interface CreateMedidaCorporalInterface {
    MedidaCorporal create(Long usuarioId, MedidaCorporal registro);
}
