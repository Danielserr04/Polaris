package com.polaris.atlas.application.in;

import com.polaris.atlas.domain.model.PesoCorporal;

/**
 * Un peso por dia: si el usuario ya tiene peso en esa fecha, se actualiza en
 * vez de crear otro (regla de Nucleo, docs/decisiones/010-registro-peso-un-peso-por-dia.md).
 */
public interface CreatePesoCorporalInterface {
    PesoCorporal create(Long usuarioId, PesoCorporal peso);
}
