package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Movimiento;

public interface RestaurarMovimientoInterface {
    Movimiento restaurar(Long usuarioId, Long id);
}
