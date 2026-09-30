package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.ResumenMensual;

import java.time.YearMonth;

public interface GetResumenMensualInterface {
    ResumenMensual get(Long usuarioId, YearMonth periodo);
}
