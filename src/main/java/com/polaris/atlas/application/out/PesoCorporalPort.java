package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.PesoCorporal;
import com.polaris.atlas.domain.model.PesoCorporalFilter;

import java.util.List;

/**
 * Lo que Atlas necesita del peso corporal, en lenguaje de Atlas. El dato no
 * es de Atlas: vive en Nucleo (tabla registro_peso) y solo el adaptador de
 * infrastructure/nucleo sabe eso. Ver docs/decisiones/022-peso-corporal-desde-fusion-y-atlas.md.
 */
public interface PesoCorporalPort {

    /** Mas reciente primero. */
    List<PesoCorporal> findAll(Long usuarioId, PesoCorporalFilter filter);

    /** Apunta el peso del dia o, si ya habia uno esa fecha, lo reemplaza. */
    PesoCorporal registrar(Long usuarioId, PesoCorporal peso);
}
