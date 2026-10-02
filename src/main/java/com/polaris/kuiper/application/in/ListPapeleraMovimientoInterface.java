package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Movimiento;

import java.util.List;

public interface ListPapeleraMovimientoInterface {
    List<Movimiento> listPapelera(Long usuarioId);
}
