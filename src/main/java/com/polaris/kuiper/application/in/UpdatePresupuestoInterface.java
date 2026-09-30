package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Presupuesto;

public interface UpdatePresupuestoInterface {
    Presupuesto update(Long usuarioId, Long id, Presupuesto presupuesto);
}
