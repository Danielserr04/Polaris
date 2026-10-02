package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.ProyeccionMensual;

import java.time.LocalDate;
import java.time.YearMonth;

public interface GetProyeccionInterface {
    /** {@code hoy} lo pone el controlador, igual que el mes actual en el resumen. */
    ProyeccionMensual get(Long usuarioId, YearMonth periodo, LocalDate hoy);
}
