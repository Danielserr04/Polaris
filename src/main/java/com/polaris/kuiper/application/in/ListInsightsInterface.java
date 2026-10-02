package com.polaris.kuiper.application.in;

import com.polaris.kuiper.domain.model.Insight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface ListInsightsInterface {
    /** {@code hoy} lo pone el controlador, igual que el mes actual en el resumen. */
    List<Insight> list(Long usuarioId, YearMonth periodo, LocalDate hoy);
}
