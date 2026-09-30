package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Movimiento;

public interface UpdateMovimientoInterface {
    Movimiento update(Long usuarioId, Long id, Movimiento movimiento);
}
