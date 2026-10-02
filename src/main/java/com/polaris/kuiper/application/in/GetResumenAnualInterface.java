package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.ResumenAnual;

public interface GetResumenAnualInterface {
    ResumenAnual get(Long usuarioId, int anio);
}
