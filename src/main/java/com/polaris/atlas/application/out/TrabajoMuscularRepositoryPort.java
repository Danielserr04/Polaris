package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.TrabajoMuscular;
import com.polaris.atlas.domain.model.TrabajoMuscularFilter;

import java.util.List;

/**
 * La agregacion por grupo muscular se hace en la base, como la progresion: el
 * dominio recibe una fila por grupo, nunca las series sueltas.
 */
public interface TrabajoMuscularRepositoryPort {

    /**
     * Una fila por grupo muscular con series del usuario dentro del rango
     * (inclusivo, extremos opcionales). Solo cuenta las series del usuario.
     */
    List<TrabajoMuscular> findTrabajo(Long usuarioId, TrabajoMuscularFilter filter);
}
