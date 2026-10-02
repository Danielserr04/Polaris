package com.polaris.nucleo.application.in;

import com.polaris.nucleo.domain.model.TipoRecordatorio;

import java.time.LocalDate;

/** "Hecho por hoy" desde la campana: no vuelve a salir ni a llegar al movil ese dia. */
public interface DescartarRecordatorioInterface {
    void descartar(Long usuarioId, TipoRecordatorio tipo, LocalDate fecha);
}
