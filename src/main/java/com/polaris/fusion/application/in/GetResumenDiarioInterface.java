package com.polaris.fusion.application.in;

import com.polaris.fusion.domain.model.ResumenDiario;

import java.time.LocalDate;

public interface GetResumenDiarioInterface {

    /** Macros consumidos ese dia contra el objetivo vigente en esa fecha (si lo hay). */
    ResumenDiario get(Long usuarioId, LocalDate fecha);
}
