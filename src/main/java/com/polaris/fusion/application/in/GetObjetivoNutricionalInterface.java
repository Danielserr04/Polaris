package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.ObjetivoNutricional;

import java.time.LocalDate;

public interface GetObjetivoNutricionalInterface {

    /** El objetivo vigente en esa fecha: el de mayor vigente_desde menor o igual a ella. */
    ObjetivoNutricional getVigente(Long usuarioId, LocalDate fecha);
}
