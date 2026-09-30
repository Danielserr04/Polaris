package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Presupuesto;

public interface CreatePresupuestoInterface {
    Presupuesto create(Long usuarioId, Presupuesto presupuesto);
}
