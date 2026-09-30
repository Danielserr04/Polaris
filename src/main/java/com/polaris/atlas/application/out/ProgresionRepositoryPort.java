package com.polaris.atlas.application.out;

import com.polaris.atlas.domain.model.ProgresionFilter;
import com.polaris.atlas.domain.model.ProgresionSesion;

import java.util.List;

/**
 * La agregacion de las series de un ejercicio por sesion se hace en la base:
 * el dominio recibe una fila por sesion, nunca las series sueltas.
 */
public interface ProgresionRepositoryPort {

    /**
     * Una fila por sesion del usuario que tenga series del ejercicio, dentro del
     * rango (inclusivo, extremos opcionales), por fecha ascendente. Solo cuenta
     * las series del usuario.
     */
    List<ProgresionSesion> findProgresion(Long usuarioId, ProgresionFilter filter);
}
