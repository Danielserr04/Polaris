package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Movimiento;
import com.polaris.kuiper.domain.model.MovimientoFilter;

import java.util.List;

public interface ListMovimientoInterface {
    List<Movimiento> list(Long usuarioId, MovimientoFilter filter);
}
