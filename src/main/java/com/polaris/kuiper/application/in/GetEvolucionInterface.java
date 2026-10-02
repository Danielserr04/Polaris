package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.EvolucionMensual;

import java.time.YearMonth;
import java.util.List;

public interface GetEvolucionInterface {
    /** {@code meses} meses terminando en {@code hasta}, del mas antiguo al mas reciente. */
    List<EvolucionMensual> get(Long usuarioId, YearMonth hasta, int meses);
}
