package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Presupuesto;

public interface GetPresupuestoInterface {
    Presupuesto get(Long usuarioId, Long id);
}
