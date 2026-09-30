package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Presupuesto;
import com.polaris.kuiper.domain.model.PresupuestoFilter;

import java.util.List;

public interface ListPresupuestoInterface {
    List<Presupuesto> list(Long usuarioId, PresupuestoFilter filter);
}
