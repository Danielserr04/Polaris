package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Movimiento;

public interface GetMovimientoInterface {
    Movimiento get(Long usuarioId, Long id);
}
