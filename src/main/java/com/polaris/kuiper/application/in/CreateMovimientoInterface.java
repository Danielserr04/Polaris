package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Movimiento;

public interface CreateMovimientoInterface {
    Movimiento create(Long usuarioId, Movimiento movimiento);
}
